package com.hcl.hackathon.scoring.repository;

import com.hcl.hackathon.scoring.entity.CreditCard;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CreditCardRepositoryTest {

    @Autowired
    private CreditCardRepository repository;

    @Test
    void shouldSaveCard() {

        CreditCard card =
                new CreditCard();

        card.setApplicationId("APP1001");
        card.setCardType("PLATINUM");
        card.setCardLimit(40000.0);

        CreditCard saved =
                repository.save(card);

        assertNotNull(saved.getId());
    }
}