package com.hcl.hackathon.scoring.service;

import com.hcl.hackathon.scoring.dto.request.CardRegistrationRequest;
import com.hcl.hackathon.scoring.dto.response.CardRegistrationResponse;
import com.hcl.hackathon.scoring.entity.CreditCard;
import com.hcl.hackathon.scoring.repository.CreditCardRepository;
import com.hcl.hackathon.scoring.service.impl.CardRegistrationServiceImpl;
import com.hcl.hackathon.scoring.util.CardUtil;
import com.hcl.hackathon.scoring.util.EncryptionUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CardRegistrationServiceImplTest {

    @Mock
    private CreditCardRepository repository;

    @Mock
    private CardUtil cardUtil;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private CardRegistrationServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldApprovePlatinumCard() {

        CardRegistrationRequest request =
                new CardRegistrationRequest();

        request.setApplicationId("APP1001");

        when(cardUtil.generateCardNumber())
                .thenReturn("4567123412341234");

        when(cardUtil.generatePin())
                .thenReturn("1234");

        when(encryptionUtil.encrypt(anyString()))
                .thenReturn("encrypted");

        CardRegistrationServiceImpl spyService =
                spy(service);

        doReturn(500)
                .when(spyService)
                .fetchCreditScore("APP1001");

        CardRegistrationResponse response =
                spyService.registerCard(request);

        assertNotNull(response);

        assertEquals(
                "PLATINUM",
                response.getCardType());

        assertEquals(
                "APPROVED",
                response.getStatus());

        assertEquals(
                40000,
                response.getCreditLimit());

        verify(repository, times(1))
                .save(any(CreditCard.class));
    }

    @Test
    void shouldReturnDocumentRequired() {

        CardRegistrationRequest request =
                new CardRegistrationRequest();

        request.setApplicationId("APP2001");

        CardRegistrationServiceImpl spyService =
                spy(service);

        doReturn(50)
                .when(spyService)
                .fetchCreditScore("APP2001");

        CardRegistrationResponse response =
                spyService.registerCard(request);

        assertEquals(
                "DOCUMENT_REQUIRED",
                response.getStatus());

        verify(repository, never())
                .save(any());
    }
}