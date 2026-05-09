package com.hcl.hackathon.registration.client;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hcl.hackathon.registration.config.RegistrationSchedulerProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Thin client wrapping the external CIBIL credit-score endpoint:
 *
 * <pre>GET {registration.scheduler.credit-score-url}/{pan}</pre>
 *
 * The endpoint returns a single integer (the credit score) for the supplied
 * PAN. Network or remote-side failures are absorbed and surfaced as
 * {@code Optional.empty()} so the scheduler can skip problematic records and
 * move on to the next one in the batch.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditScoreClient {

    private final RestTemplate restTemplate;
    private final RegistrationSchedulerProperties properties;

    /**
     * Fetch the CIBIL score for the supplied PAN.
     *
     * @param pan applicant's PAN (used as the path variable)
     * @return score wrapped in Optional; empty when PAN is missing or the
     *         remote call fails / returns null
     */
    public Optional<Integer> fetchCreditScore(String pan) {
        if (pan == null || pan.isBlank()) {
            log.warn("Skipping credit-score lookup — PAN is null/blank");
            return Optional.empty();
        }

        String url = properties.getCreditScoreUrl() + "/{pan}";
        log.debug("Calling CIBIL API: {} with pan: {}", url, pan);
        try {
            Integer score = restTemplate.getForObject(url, Integer.class, pan);
            if (score == null) {
                log.warn("CIBIL API returned null body for pan: {}", pan);
                return Optional.empty();
            }
            log.debug("CIBIL API returned score {} for pan: {}", score, pan);
            return Optional.of(score);
        } catch (RestClientException ex) {
            log.warn("CIBIL API call failed for pan: {} — {}", pan, ex.getMessage());
            return Optional.empty();
        }
    }
}
