package com.zbank.customerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_audit_log")
public class CardAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private CreditCard card;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "event_payload", length = 500)
    private String eventPayload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static CardAuditLog pinGenerated(CreditCard card) {
        CardAuditLog log = new CardAuditLog();
        log.card = card;
        log.eventType = "PIN_GENERATED";
        log.eventPayload = "Card activated through first login";
        log.createdAt = LocalDateTime.now();
        return log;
    }
}
