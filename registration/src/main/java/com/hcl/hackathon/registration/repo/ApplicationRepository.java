package com.hcl.hackathon.registration.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcl.hackathon.registration.entity.CreditCardApplication;
import com.hcl.hackathon.registration.util.ApplicationStatus;

@Repository
public interface ApplicationRepository extends JpaRepository<CreditCardApplication, Long> {

    Optional<CreditCardApplication> findByApplicationReference(String reference);

    /**
     * Returns the oldest application currently in the given status — used to
     * dispatch the next pending application to a worker / scoring service.
     */
    Optional<CreditCardApplication> findFirstByStatusOrderByCreatedAtAsc(ApplicationStatus status);

    /**
     * Returns a batch of the oldest applications in the given status that have
     * not yet been scored. Drives the credit-score scheduler.
     */
    List<CreditCardApplication> findByStatusAndCreditScoreIsNullOrderByCreatedAtAsc(
            ApplicationStatus status, Pageable pageable);
}
