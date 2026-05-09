package com.hcl.hackathon.scoring.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "credit_card")
@Data
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String applicationId;

    private String cardType;

    private Double cardLimit;

    private String cardNumberEncrypted;

    private String maskedCardNumber;

    private String pinEncrypted;

    private String status;
}