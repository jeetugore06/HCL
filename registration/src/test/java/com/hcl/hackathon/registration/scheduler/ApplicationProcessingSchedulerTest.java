package com.hcl.hackathon.registration.scheduler;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hcl.hackathon.registration.client.CreditScoreClient;
import com.hcl.hackathon.registration.config.RegistrationSchedulerProperties;
import com.hcl.hackathon.registration.dto.ApplicationDetailDTO;
import com.hcl.hackathon.registration.service.RegistrationService;

/**
 * Scheduler-orchestration tests — pure JUnit 5 + Mockito; no Spring context.
 * Covers happy path, empty queue, CIBIL lookup failure, and persist failure.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationProcessingSchedulerTest {

    @Mock
    private RegistrationService registrationService;

    @Mock
    private CreditScoreClient creditScoreClient;

    private RegistrationSchedulerProperties properties;
    private ApplicationProcessingScheduler scheduler;

    @BeforeEach
    void setUp() {
        properties = new RegistrationSchedulerProperties();
        properties.setBatchSize(3);
        scheduler = new ApplicationProcessingScheduler(properties, registrationService, creditScoreClient);
    }

    private static ApplicationDetailDTO app(long id, String pan) {
        return ApplicationDetailDTO.builder().id(id).documentId(pan).documentType("PAN").build();
    }

    @Test
    @DisplayName("Empty queue: makes no CIBIL calls and no updates")
    void emptyQueue_noWork() {
        when(registrationService.getPendingApplications(3)).thenReturn(List.of());

        scheduler.processPendingApplications();

        verify(creditScoreClient, never()).fetchCreditScore(anyString());
        verify(registrationService, never()).applyCreditScore(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Happy path: each app's PAN is sent to CIBIL and the returned score is persisted")
    void happyPath_allScored() {
        ApplicationDetailDTO a1 = app(1L, "ABCDE1234F");
        ApplicationDetailDTO a2 = app(2L, "ZXCVB9876K");
        when(registrationService.getPendingApplications(3)).thenReturn(List.of(a1, a2));
        when(creditScoreClient.fetchCreditScore("ABCDE1234F")).thenReturn(Optional.of(720));
        when(creditScoreClient.fetchCreditScore("ZXCVB9876K")).thenReturn(Optional.of(810));

        scheduler.processPendingApplications();

        verify(creditScoreClient).fetchCreditScore("ABCDE1234F");
        verify(creditScoreClient).fetchCreditScore("ZXCVB9876K");
        verify(registrationService).applyCreditScore(1L, 720);
        verify(registrationService).applyCreditScore(2L, 810);
    }

    @Test
    @DisplayName("CIBIL lookup failure: that single app is skipped, others continue")
    void scoreLookupFails_appSkipped() {
        ApplicationDetailDTO a1 = app(1L, "ABCDE1234F");
        ApplicationDetailDTO a2 = app(2L, "ZXCVB9876K");
        when(registrationService.getPendingApplications(3)).thenReturn(List.of(a1, a2));
        when(creditScoreClient.fetchCreditScore("ABCDE1234F")).thenReturn(Optional.empty());
        when(creditScoreClient.fetchCreditScore("ZXCVB9876K")).thenReturn(Optional.of(810));

        scheduler.processPendingApplications();

        verify(registrationService, never()).applyCreditScore(eq(1L), anyInt());
        verify(registrationService).applyCreditScore(2L, 810);
    }

    @Test
    @DisplayName("Persist failure on one app does not abort the batch")
    void persistFails_otherAppsStillProcessed() {
        ApplicationDetailDTO a1 = app(1L, "ABCDE1234F");
        ApplicationDetailDTO a2 = app(2L, "ZXCVB9876K");
        when(registrationService.getPendingApplications(3)).thenReturn(List.of(a1, a2));
        when(creditScoreClient.fetchCreditScore("ABCDE1234F")).thenReturn(Optional.of(720));
        when(creditScoreClient.fetchCreditScore("ZXCVB9876K")).thenReturn(Optional.of(810));
        when(registrationService.applyCreditScore(1L, 720))
                .thenThrow(new RuntimeException("DB blip"));

        scheduler.processPendingApplications();

        verify(registrationService, times(1)).applyCreditScore(1L, 720);
        verify(registrationService, times(1)).applyCreditScore(2L, 810);
    }
}
