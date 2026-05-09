package com.zbank.customerservice.repository;

import com.zbank.customerservice.entity.CardAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardAuditLogRepository extends JpaRepository<CardAuditLog, Long> {
}
