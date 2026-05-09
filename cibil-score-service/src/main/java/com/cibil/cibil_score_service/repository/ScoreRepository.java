
package com.cibil.repository;

import com.cibil.entity.Cibil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository
        extends JpaRepository<Cibil, Long> {
}