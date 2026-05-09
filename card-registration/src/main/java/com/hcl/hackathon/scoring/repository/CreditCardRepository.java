package com.hcl.hackathon.scoring.repository;

import com.hcl.hackathon.scoring.entity.CreditCard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CreditCardRepository
        extends JpaRepository<CreditCard, Long> {
}