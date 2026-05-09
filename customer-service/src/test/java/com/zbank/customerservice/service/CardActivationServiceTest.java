package com.zbank.customerservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zbank.customerservice.dto.CardActivationRequest;
import com.zbank.customerservice.dto.CardActivationResponse;
import com.zbank.customerservice.dto.GetUserResponse;
import com.zbank.customerservice.entity.CreditCard;
import com.zbank.customerservice.entity.Customer;
import com.zbank.customerservice.entity.PinStatus;
import com.zbank.customerservice.exception.CardAlreadyActivatedException;
import com.zbank.customerservice.exception.InvalidActivationDetailsException;
import com.zbank.customerservice.exception.ResourceNotFoundException;
import com.zbank.customerservice.repository.CardAuditLogRepository;
import com.zbank.customerservice.repository.CreditCardRepository;
import com.zbank.customerservice.repository.CustomerRepository;
import com.zbank.customerservice.security.JwtTokenProvider;
import com.zbank.customerservice.support.TestDataFactory;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CardActivationServiceTest {

    @Mock
    private CreditCardRepository creditCardRepository;
    @Mock
    private CardAuditLogRepository cardAuditLogRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private CardActivationService cardActivationService;

    private CardActivationRequest request;
    private CreditCard pendingCard;

    @BeforeEach
    void setUp() {
        request = TestDataFactory.validActivationRequest();
        Customer customer = TestDataFactory.customer(42L, "Sohum", "Shah", "DOC-123");
        pendingCard = TestDataFactory.pendingCard(customer, "old-hash");
    }

    @Test
    void activateCard_shouldThrowWhenCardNotFound() {
        when(creditCardRepository.findByCardNumber(request.creditCardNumber())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cardActivationService.activateCard(request));
    }

    @Test
    void activateCard_shouldThrowWhenCardAlreadyActivated() {
        ReflectionTestUtils.setField(pendingCard, "pinStatus", PinStatus.ACTIVATED);
        when(creditCardRepository.findByCardNumber(request.creditCardNumber())).thenReturn(Optional.of(pendingCard));

        assertThrows(CardAlreadyActivatedException.class, () -> cardActivationService.activateCard(request));
    }

    @Test
    void activateCard_shouldThrowWhenDocumentDoesNotMatch() {
        Customer differentCustomer = TestDataFactory.customer(42L, "Sohum", "Shah", "DOC-OTHER");
        ReflectionTestUtils.setField(pendingCard, "customer", differentCustomer);
        when(creditCardRepository.findByCardNumber(request.creditCardNumber())).thenReturn(Optional.of(pendingCard));
        when(passwordEncoder.matches(request.pin(), "old-hash")).thenReturn(true);

        assertThrows(InvalidActivationDetailsException.class, () -> cardActivationService.activateCard(request));
    }

    @Test
    void activateCard_shouldThrowWhenPinDoesNotMatch() {
        when(creditCardRepository.findByCardNumber(request.creditCardNumber())).thenReturn(Optional.of(pendingCard));
        when(passwordEncoder.matches(request.pin(), "old-hash")).thenReturn(false);

        assertThrows(InvalidActivationDetailsException.class, () -> cardActivationService.activateCard(request));
    }

    @Test
    void activateCard_shouldThrowWhenNewPinsDoNotMatch() {
        CardActivationRequest invalid = new CardActivationRequest(
                request.creditCardNumber(),
                request.pin(),
                request.documentNumber(),
                "5678",
                "9999"
        );
        when(creditCardRepository.findByCardNumber(request.creditCardNumber())).thenReturn(Optional.of(pendingCard));
        when(passwordEncoder.matches(request.pin(), "old-hash")).thenReturn(true);

        assertThrows(InvalidActivationDetailsException.class, () -> cardActivationService.activateCard(invalid));
    }

    @Test
    void activateCard_shouldThrowWhenNewPinSameAsOldPin() {
        CardActivationRequest invalid = new CardActivationRequest(
                request.creditCardNumber(),
                request.pin(),
                request.documentNumber(),
                request.pin(),
                request.pin()
        );
        when(creditCardRepository.findByCardNumber(request.creditCardNumber())).thenReturn(Optional.of(pendingCard));
        when(passwordEncoder.matches(request.pin(), "old-hash")).thenReturn(true);

        assertThrows(InvalidActivationDetailsException.class, () -> cardActivationService.activateCard(invalid));
    }

    @Test
    void activateCard_shouldActivateCardAndReturnToken() {
        when(creditCardRepository.findByCardNumber(request.creditCardNumber())).thenReturn(Optional.of(pendingCard));
        when(passwordEncoder.matches(request.pin(), "old-hash")).thenReturn(true);
        when(passwordEncoder.encode(request.newPin())).thenReturn("new-hash");
        when(jwtTokenProvider.generateToken(42L, request.creditCardNumber())).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationSeconds()).thenReturn(3600L);

        CardActivationResponse response = cardActivationService.activateCard(request);

        assertEquals("Card activated successfully", response.message());
        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        assertEquals(PinStatus.ACTIVATED, pendingCard.getPinStatus());
        assertEquals("new-hash", pendingCard.getPinHash());
        assertTrue(ReflectionTestUtils.getField(pendingCard, "updatedAt") != null);
        verify(cardAuditLogRepository).save(any());
    }

    @Test
    void getUserFromToken_shouldThrowWhenTokenBlank() {
        assertThrows(InvalidActivationDetailsException.class, () -> cardActivationService.getUserFromToken(" "));
    }

    @Test
    void getUserFromToken_shouldSupportBearerToken() {
        when(jwtTokenProvider.extractCustomerId("abc")).thenReturn(42L);
        Customer customer = TestDataFactory.customer(42L, "Sohum", "Shah", "DOC-123");
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));

        GetUserResponse response = cardActivationService.getUserFromToken("Bearer abc");

        assertEquals("Sohum Shah", response.customerName());
    }

    @Test
    void getUserFromToken_shouldSupportRawToken() {
        when(jwtTokenProvider.extractCustomerId("raw-token")).thenReturn(42L);
        Customer customer = TestDataFactory.customer(42L, "Sohum", "Shah", "DOC-123");
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));

        GetUserResponse response = cardActivationService.getUserFromToken("raw-token");

        assertEquals("Sohum Shah", response.customerName());
    }

    @Test
    void getUserFromToken_shouldThrowWhenCustomerNotFound() {
        when(jwtTokenProvider.extractCustomerId("abc")).thenReturn(42L);
        when(customerRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cardActivationService.getUserFromToken("Bearer abc"));
    }

    @Test
    void getUserFromToken_shouldPropagateTokenParsingErrors() {
        when(jwtTokenProvider.extractCustomerId(eq("bad"))).thenThrow(new IllegalArgumentException("Invalid or expired token"));

        assertThrows(IllegalArgumentException.class, () -> cardActivationService.getUserFromToken("Bearer bad"));
        verify(customerRepository, never()).findById(any());
    }
}
