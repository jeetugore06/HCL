package com.zbank.customerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_card")
public class CreditCard {

    @Id
    private Long id;

    @Column(name = "card_number", nullable = false, columnDefinition = "CHAR(16)")
    private String cardNumber;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "card_type", nullable = false, length = 20)
    private String cardType;

    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit;

    @Column(name = "pin_hash", nullable = false, length = 60)
    private String pinHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "pin_status", nullable = false, length = 20)
    private PinStatus pinStatus;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public Long getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public String getCardType() {
        return cardType;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public String getPinHash() {
        return pinHash;
    }

    public PinStatus getPinStatus() {
        return pinStatus;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public void setPinStatus(PinStatus pinStatus) {
        this.pinStatus = pinStatus;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Long getVersion() {
        return version;
    }
}
