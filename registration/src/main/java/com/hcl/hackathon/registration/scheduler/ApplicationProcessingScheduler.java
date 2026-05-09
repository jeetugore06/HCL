package com.hcl.hackathon.registration.scheduler;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hcl.hackathon.registration.client.CibilCheckRequest;
import com.hcl.hackathon.registration.client.CreditScoreClient;
import com.hcl.hackathon.registration.config.RegistrationSchedulerProperties;
import com.hcl.hackathon.registration.dto.ApplicationDetailDTO;
import com.hcl.hackathon.registration.service.RegistrationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Periodically pulls a batch of pending applications, asks the external
 * credit-score service for a score per application, and persists the score
 * back onto the entity.
 *
 * Activation and cadence are fully driven by configuration:
 *
 * <pre>
 * registration.scheduler.enabled=true
 * registration.scheduler.cron=0 *&#47;1 * * * *
 * registration.scheduler.batch-size=10
 * registration.scheduler.credit-score-url=...
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "registration.scheduler",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ApplicationProcessingScheduler {

    /** TODO: replace with a call to the cards-held service when available. */
    private static final int DEFAULT_TOTAL_CARDS = 0;

    private final RegistrationSchedulerProperties properties;
    private final RegistrationService registrationService;
    private final CreditScoreClient creditScoreClient;

    @Scheduled(cron = "${registration.scheduler.cron:0 */1 * * * *}")
    public void processPendingApplications() {
        int batchSize = properties.getBatchSize();
        log.info("Credit-score scheduler tick — pulling up to {} pending applications", batchSize);

        List<ApplicationDetailDTO> pending = registrationService.getPendingApplications(batchSize);
        if (pending.isEmpty()) {
            log.info("No pending applications to score");
            return;
        }

        int processed = 0;
        int skipped = 0;
        for (ApplicationDetailDTO app : pending) {
            CibilCheckRequest request = CibilCheckRequest.builder()
                    .panNo(app.getDocumentId())
                    .annualSalary(app.getAnnualSalary())
                    .totalCards(DEFAULT_TOTAL_CARDS)
                    .build();

            var maybeScore = creditScoreClient.fetchCreditScore(request);
            if (maybeScore.isEmpty()) {
                log.warn("Skipping application id: {} — CIBIL lookup failed for pan: {}",
                        app.getId(), request.getPanNo());
                skipped++;
                continue;
            }
            try {
                registrationService.applyCreditScore(app.getId(), maybeScore.get());
                processed++;
            } catch (RuntimeException ex) {
                log.warn("Failed to persist credit score for application id: {} — {}",
                        app.getId(), ex.getMessage());
                skipped++;
            }
        }
        log.info("Credit-score scheduler tick complete — processed: {}, skipped: {}", processed, skipped);
    }
}
