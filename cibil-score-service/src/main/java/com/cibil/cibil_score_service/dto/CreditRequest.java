package com.cibil.cibil_score_service.dto;

import lombok.Data;

@Data
public class CreditRequest {

    private String panNo;

    private Double annualSalary;

    private Integer totalCards;
}