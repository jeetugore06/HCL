
package com.cibil.repository;

import com.cibil.entity.Credit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditRepository
        extends JpaRepository<Credit, Long> {
}