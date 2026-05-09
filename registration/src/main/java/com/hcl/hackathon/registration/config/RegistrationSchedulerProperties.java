package com.hcl.hackathon.registration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration for the credit-score scheduler.
 *
 * Bound to property prefix <code>registration.scheduler</code>.
 *
 * <pre>
 * registration.scheduler.enabled=true
 * registration.scheduler.cron=0 *&#47;1 * * * *
 * registration.scheduler.batch-size=10
 * registration.scheduler.credit-score-url=http://localhost:8081/api/v1/credit/score
 * registration.scheduler.connect-timeout-ms=2000
 * registration.scheduler.read-timeout-ms=5000
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "registration.scheduler")
public class RegistrationSchedulerProperties {

    /** Master switch — set to false to disable the scheduler entirely. */
    private boolean enabled = true;

    /** Cron expression driving the scheduler. Default: every minute. */
    private String cron = "0 */1 * * * *";

    /** Maximum number of pending applications to score per tick. */
    private int batchSize = 10;

    /**
     * Endpoint of the external CIBIL service.
     * Called via {@code POST} with a JSON body of
     * {@code panNo, annualSalary, totalCards}; returns a single integer score.
     */
    private String creditScoreUrl = "http://localhost:8081/api/cibil/check";

    /** HTTP connect timeout for credit-score calls (milliseconds). */
    private long connectTimeoutMs = 2_000L;

    /** HTTP read timeout for credit-score calls (milliseconds). */
    private long readTimeoutMs = 5_000L;
}
