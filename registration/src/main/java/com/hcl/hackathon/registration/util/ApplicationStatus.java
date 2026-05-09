package com.hcl.hackathon.registration.util;

public enum ApplicationStatus {
    /**
     * Application has been received and persisted.
     */
    SUBMITTED,

    /**
     * Application is currently being evaluated by the scoring-service.
     */
    PROCESSING,

    /**
     * Application passed credit checks; card-service can now issue the card.
     */
    APPROVED,

    /**
     * Application failed credit checks or salary requirements.
     */
    REJECTED,

    /**
     * Score was 50; workflow is paused for manual document review (Out of Scope).
     */
    ADDITIONAL_DOCUMENTS_REQUESTED
}
