package com.hcl.hackathon.registration.client;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hcl.hackathon.registration.config.RegistrationSchedulerProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Thin client wrapping the external CIBIL credit-check endpoint:
 *
 * <pre>POST {registration.scheduler.credit-score-url}</pre>
 *
 * The endpoint accepts a {@link CibilCheckRequest} JSON body and returns a
 * single integer (the credit score). Network or remote-side failures are
 * absorbed and surfaced as {@code Optional.empty()} so the scheduler can skip
 * problematic records and move on to the next one in the batch.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditScoreClient {

    private final RestTemplate restTemplate;
    private final RegistrationSchedulerProperties properties;

    /**
     * Fetch the CIBIL score for the supplied request.
     *
     * @param request body to POST
     * @return score wrapped in Optional; empty when the request is missing/invalid
     *         or the remote call fails / returns null
     */
    public Optional<Integer> fetchCreditScore(CibilCheckRequest request) {
        if (request == null || request.getPanNo() == null || request.getPanNo().isBlank()) {
            log.warn("Skipping CIBIL lookup — request or PAN is null/blank");
            return Optional.empty();
        }

        String url = properties.getCreditScoreUrl();
        log.debug("Calling CIBIL API: {} with payload: {}", url, request);
        try {
            Integer score = restTemplate.postForObject(url, request, Integer.class);
            if (score == null) {
                log.warn("CIBIL API returned null body for pan: {}", request.getPanNo());
                return Optional.empty();
            }
            log.debug("CIBIL API returned score {} for pan: {}", score, request.getPanNo());
            return Optional.of(score);
        } catch (RestClientException ex) {
            log.warn("CIBIL API call failed for pan: {} — {}", request.getPanNo(), ex.getMessage());
            return Optional.empty();
        }
    }
}
