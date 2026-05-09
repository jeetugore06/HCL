package com.hcl.hackathon.scoring.utility;

public class MaskingUtil {

    public static String maskCard(
            String cardNumber) {

        return "XXXX-XXXX-XXXX-"
                + cardNumber.substring(12);
    }
}