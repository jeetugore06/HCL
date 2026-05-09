package com.hcl.hackathon.scoring.utility;

import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

    public String encrypt(String value) {

        // AES encryption logic
        return "ENCRYPTED_" + value;
    }
}