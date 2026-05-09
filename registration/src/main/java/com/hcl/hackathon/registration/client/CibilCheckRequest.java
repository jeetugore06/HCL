package com.hcl.hackathon.registration.client;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body sent to the CIBIL credit-check endpoint:
 *
 * <pre>POST {registration.scheduler.credit-score-url}</pre>
 *
 * Field names line up with what the CIBIL service expects.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CibilCheckRequest {

    /** Applicant's PAN. */
    private String panNo;

    /** Applicant's declared annual salary. */
    private BigDecimal annualSalary;

    /**
     * Number of credit cards the applicant currently holds.
     * Hardcoded to 0 in the scheduler for now — will be sourced from another
     * service later.
     */
    private int totalCards;
}
