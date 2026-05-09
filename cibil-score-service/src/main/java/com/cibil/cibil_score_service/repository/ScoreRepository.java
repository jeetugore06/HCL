
package com.cibil.cibil_score_service.repository;

import com.cibil.cibil_score_service.entity.Cibil;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ScoreRepository
        extends JpaRepository<Cibil, Long> {
        Optional<Cibil> findByPanNo(String panNo);
}