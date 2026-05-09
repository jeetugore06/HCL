package com.hcl.hackathon.scoring.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardUtilTest {

    private final CardUtil cardUtil =
            new CardUtil();

    @Test
    void shouldGenerate16DigitCard() {

        String cardNumber =
                cardUtil.generateCardNumber();

        assertNotNull(cardNumber);

        assertEquals(
                16,
                cardNumber.length());
    }

    @Test
    void shouldGenerate4DigitPin() {

        String pin =
                cardUtil.generatePin();

        assertEquals(
                4,
                pin.length());
    }
}