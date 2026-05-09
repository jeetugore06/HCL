package com.zbank.customerservice.repository;

import com.zbank.customerservice.entity.CreditCard;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    @EntityGraph(attributePaths = "customer")
    Optional<CreditCard> findByCardNumber(String cardNumber);
}
