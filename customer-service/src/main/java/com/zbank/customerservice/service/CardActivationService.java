package com.zbank.customerservice.service;

import com.zbank.customerservice.dto.CardActivationRequest;
import com.zbank.customerservice.dto.CardActivationResponse;
import com.zbank.customerservice.dto.GetUserResponse;
import com.zbank.customerservice.entity.CardAuditLog;
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
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardActivationService {

    private final CreditCardRepository creditCardRepository;
    private final CardAuditLogRepository cardAuditLogRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public CardActivationService(
            CreditCardRepository creditCardRepository,
            CardAuditLogRepository cardAuditLogRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.creditCardRepository = creditCardRepository;
        this.cardAuditLogRepository = cardAuditLogRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public CardActivationResponse activateCard(CardActivationRequest request) {
        //validateNewPinPair(request);

        CreditCard creditCard = creditCardRepository.findByCardNumber(request.creditCardNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Credit card not found"));

        if (creditCard.getPinStatus() != PinStatus.PENDING_ACTIVATION) {
            throw new CardAlreadyActivatedException("Card is already activated");
        }

        boolean matchingDocument = creditCard.getCustomer().getDocumentId().equals(request.documentNumber());
        boolean matchingPin = passwordEncoder.matches(request.pin(), creditCard.getPinHash());
        if (!matchingDocument || !matchingPin) {
            throw new InvalidActivationDetailsException("Card number, PIN, or document number is invalid");
        }

        validateNewPinPair(request);

        creditCard.setPinHash(passwordEncoder.encode(request.newPin()));
        creditCard.setPinStatus(PinStatus.ACTIVATED);
        creditCard.setUpdatedAt(LocalDateTime.now());
        cardAuditLogRepository.save(CardAuditLog.pinGenerated(creditCard));

        String accessToken = jwtTokenProvider.generateToken(creditCard.getCustomer().getId(), creditCard.getCardNumber());
        return new CardActivationResponse(
                "Card activated successfully",
                accessToken,
                "Bearer",
                jwtTokenProvider.getExpirationSeconds()
        );
    }

    @Transactional(readOnly = true)
    public GetUserResponse getUserFromToken(String authToken) {
        String token = resolveToken(authToken);
        Long customerId = jwtTokenProvider.extractCustomerId(token);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for token"));

        return new GetUserResponse(customer.getFirstName() + " " + customer.getLastName());
    }

    private static void validateNewPinPair(CardActivationRequest request) {
        if (!request.newPin().equals(request.newPinConfirmed())) {
            throw new InvalidActivationDetailsException("newPin and newPinConfirmed do not match");
        }
        if (request.newPin().equals(request.pin())) {
            throw new InvalidActivationDetailsException("newPin must be different from current PIN");
        }
    }

    private String resolveToken(String authToken) {
        if (authToken == null || authToken.isBlank()) {
            throw new InvalidActivationDetailsException("JWT token is required");
        }
        if (authToken.startsWith("Bearer ")) {
            return authToken.substring(7);
        }
        return authToken;
    }
}
