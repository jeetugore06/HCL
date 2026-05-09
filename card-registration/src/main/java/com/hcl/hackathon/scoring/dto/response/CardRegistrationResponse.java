package com.hcl.hackathon.scoring.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardRegistrationResponse {

    private String applicationId;

    private String status;

    private String cardType;

    private Double creditLimit;

    private String maskedCardNumber;

}