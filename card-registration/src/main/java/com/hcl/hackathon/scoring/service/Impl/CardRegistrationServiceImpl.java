package com.hcl.hackathon.scoring.service.impl;

import com.hcl.hackathon.scoring.dto.request.CardRegistrationRequest;
import com.hcl.hackathon.scoring.dto.response.CardRegistrationResponse;
import com.hcl.hackathon.scoring.entity.CreditCard;
import com.hcl.hackathon.scoring.enums.ApplicationStatus;
import com.hcl.hackathon.scoring.enums.CardType;
import com.hcl.hackathon.scoring.repository.CreditCardRepository;
import com.hcl.hackathon.scoring.service.CardRegistrationService;
import com.hcl.hackathon.scoring.util.CardUtil;
import com.hcl.hackathon.scoring.util.EncryptionUtil;
import com.hcl.hackathon.scoring.util.MaskingUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardRegistrationServiceImpl
        implements CardRegistrationService {

    private final CreditCardRepository repository;

    private final CardUtil cardUtil;

    private final EncryptionUtil encryptionUtil;

    @Override
    public CardRegistrationResponse registerCard(
            CardRegistrationRequest request) {

        int creditScore = fetchCreditScore(
                request.getApplicationId());

        if (creditScore == 50) {

            return CardRegistrationResponse.builder()
                    .applicationId(request.getApplicationId())
                    .status(
                            ApplicationStatus
                                    .DOCUMENT_REQUIRED
                                    .name())
                    .build();
        }

        CardType cardType;
        double limit;

        if (creditScore >= 500) {

            cardType = CardType.PLATINUM;
            limit = 40000;

        } else if (creditScore >= 300) {

            cardType = CardType.GOLD;
            limit = 20000;

        } else {

            cardType = CardType.VISA;
            limit = 10000;
        }

        String cardNumber =
                cardUtil.generateCardNumber();

        String maskedCard =
                MaskingUtil.maskCard(cardNumber);

        String pin =
                cardUtil.generatePin();

        CreditCard card = new CreditCard();

        card.setApplicationId(
                request.getApplicationId());

        card.setCardType(cardType.name());

        card.setCardLimit(limit);

        card.setMaskedCardNumber(maskedCard);

        card.setCardNumberEncrypted(
                encryptionUtil.encrypt(cardNumber));

        card.setPinEncrypted(
                encryptionUtil.encrypt(pin));

        card.setStatus(
                ApplicationStatus.APPROVED.name());

        repository.save(card);

        return CardRegistrationResponse.builder()
                .applicationId(
                        request.getApplicationId())
                .status(
                        ApplicationStatus.APPROVED.name())
                .cardType(cardType.name())
                .creditLimit(limit)
                .maskedCardNumber(maskedCard)
                .build();
    }

    private int fetchCreditScore(
            String applicationId) {

        // Call external credit score service
        return 500;
    }
}