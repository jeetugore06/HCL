package com.hcl.hackathon.scoring.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CardUtil {

    public String generateCardNumber() {

        StringBuilder builder =
                new StringBuilder("456712");

        Random random = new Random();

        while (builder.length() < 16) {
            builder.append(random.nextInt(10));
        }

        return builder.toString();
    }

    public String generatePin() {

        Random random = new Random();

        int pin =
                1000 + random.nextInt(9000);

        return String.valueOf(pin);
    }
}