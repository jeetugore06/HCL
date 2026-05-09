package com.hcl.hackathon.registration.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcl.hackathon.registration.dto.ApplicationDetailDTO;
import com.hcl.hackathon.registration.dto.ApplicationRequestDTO;
import com.hcl.hackathon.registration.dto.ApplicationResponseDTO;
import com.hcl.hackathon.registration.entity.CreditCardApplication;
import com.hcl.hackathon.registration.exception.ResourceNotFoundException;
import com.hcl.hackathon.registration.mapper.ApplicationMapper;
import com.hcl.hackathon.registration.repo.ApplicationRepository;
import com.hcl.hackathon.registration.util.ApplicationStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final ApplicationRepository repository;
    private final ApplicationMapper mapper;

    /** CREATE — persist a new application and return a short ack. */
    @Transactional
    public ApplicationResponseDTO processApplication(ApplicationRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Application request payload must not be null");
        }
        log.info("Creating new credit-card application for email: {}", dto.getEmail());

        CreditCardApplication saved = repository.save(mapper.toEntity(dto));

        log.info("Created application id: {} reference: {}", saved.getId(), saved.getApplicationReference());
        return mapper.toResponseDTO(saved);
    }

    /** READ ALL. */
    @Transactional(readOnly = true)
    public List<ApplicationDetailDTO> getAllApplications() {
        log.info("Fetching all credit-card applications");
        return repository.findAll().stream()
                .map(mapper::toDetailDTO)
                .toList();
    }

    /** READ BY ID. */
    @Transactional(readOnly = true)
    public ApplicationDetailDTO getApplicationById(Long id) {
        log.info("Fetching credit-card application by id: {}", id);
        return repository.findById(id)
                .map(mapper::toDetailDTO)
                .orElseThrow(() -> {
                    log.warn("Application not found by id: {}", id);
                    return new ResourceNotFoundException("Application not found with id: " + id);
                });
    }

    /** READ BY APPLICATION REFERENCE. */
    @Transactional(readOnly = true)
    public ApplicationDetailDTO getApplicationByReference(String reference) {
        log.info("Fetching credit-card application by reference: {}", reference);
        return repository.findByApplicationReference(reference)
                .map(mapper::toDetailDTO)
                .orElseThrow(() -> {
                    log.warn("Application not found by reference: {}", reference);
                    return new ResourceNotFoundException(
                            "Application not found with reference: " + reference);
                });
    }

    /** UPDATE — apply mutable fields from the request onto an existing record. */
    @Transactional
    public ApplicationDetailDTO updateApplication(Long id, ApplicationRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Application request payload must not be null");
        }
        log.info("Updating credit-card application id: {}", id);

        CreditCardApplication entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update aborted; application not found by id: {}", id);
                    return new ResourceNotFoundException("Application not found with id: " + id);
                });

        mapper.updateEntity(entity, dto);
        CreditCardApplication saved = repository.save(entity);

        log.info("Updated application id: {} reference: {}", saved.getId(), saved.getApplicationReference());
        return mapper.toDetailDTO(saved);
    }

    /**
     * Returns the next application waiting to be processed (status = SUBMITTED, oldest first).
     * Empty Optional when there is no pending work — the controller maps that to 204 No Content.
     */
    @Transactional(readOnly = true)
    public Optional<ApplicationDetailDTO> getNextPendingApplication() {
        log.info("Fetching next pending credit-card application (status={})", ApplicationStatus.SUBMITTED);
        Optional<ApplicationDetailDTO> next = repository
                .findFirstByStatusOrderByCreatedAtAsc(ApplicationStatus.SUBMITTED)
                .map(mapper::toDetailDTO);

        next.ifPresentOrElse(
                dto -> log.info("Next pending application id: {} reference: {}",
                        dto.getId(), dto.getApplicationReference()),
                () -> log.info("No pending applications in queue"));

        return next;
    }

    /**
     * Returns a batch of unscored SUBMITTED applications — drives the
     * credit-score scheduler.
     */
    @Transactional(readOnly = true)
    public List<ApplicationDetailDTO> getPendingApplications(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be positive, got: " + batchSize);
        }
        log.info("Fetching pending applications batch (size={})", batchSize);
        return repository
                .findByStatusAndCreditScoreIsNullOrderByCreatedAtAsc(
                        ApplicationStatus.SUBMITTED, PageRequest.of(0, batchSize))
                .stream()
                .map(mapper::toDetailDTO)
                .toList();
    }

    /**
     * Persists the credit score returned by the external scoring service onto
     * an existing application. Each call runs in its own transaction so that
     * a failure on one record does not roll back others in the same batch.
     */
    @Transactional
    public ApplicationDetailDTO applyCreditScore(Long id, int score) {
        log.info("Applying credit score {} to application id: {}", score, id);
        CreditCardApplication entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Apply credit score aborted; application not found by id: {}", id);
                    return new ResourceNotFoundException("Application not found with id: " + id);
                });
        entity.setCreditScore(score);
        CreditCardApplication saved = repository.save(entity);
        log.info("Credit score persisted for application id: {} reference: {}",
                saved.getId(), saved.getApplicationReference());
        return mapper.toDetailDTO(saved);
    }

    /** DELETE. */
    @Transactional
    public void deleteApplication(Long id) {
        log.info("Deleting credit-card application id: {}", id);
        if (!repository.existsById(id)) {
            log.warn("Delete aborted; application not found by id: {}", id);
            throw new ResourceNotFoundException("Application not found with id: " + id);
        }
        repository.deleteById(id);
        log.info("Deleted application id: {}", id);
    }
}
