package com.zbank.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CardActivationRequest(
        @NotBlank
        @Pattern(regexp = "\\d{16}", message = "creditCardNumber must be 16 digits")
        String creditCardNumber,

        @NotBlank
        @Pattern(regexp = "\\d{4}", message = "pin must be 4 digits")
        String pin,

        @NotBlank
        @Size(max = 40)
        String documentNumber,

        @NotBlank
        @Pattern(regexp = "\\d{4}", message = "newPin must be 4 digits")
        String newPin,

        @NotBlank
        @Pattern(regexp = "\\d{4}", message = "newPinConfirmed must be 4 digits")
        String newPinConfirmed
) {
}
