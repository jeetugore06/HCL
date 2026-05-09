package com.zbank.customerservice.dto;

public record CardActivationResponse(
        String message,
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
