package com.hcl.hackathon.registration.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hcl.hackathon.registration.client.CibilCheckRequest;
import com.hcl.hackathon.registration.client.CreditScoreClient;
import com.hcl.hackathon.registration.config.RegistrationSchedulerProperties;
import com.hcl.hackathon.registration.dto.ApplicationDetailDTO;
import com.hcl.hackathon.registration.service.RegistrationService;

/**
 * Scheduler-orchestration tests — pure JUnit 5 + Mockito; no Spring context.
 * Covers happy path, empty queue, CIBIL lookup failure, and persist failure,
 * and asserts the request body sent to CIBIL.
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

    private static ApplicationDetailDTO app(long id, String pan, String salary) {
        return ApplicationDetailDTO.builder()
                .id(id)
                .documentId(pan)
                .documentType("PAN")
                .annualSalary(new BigDecimal(salary))
                .build();
    }

    @Test
    @DisplayName("Empty queue: makes no CIBIL calls and no updates")
    void emptyQueue_noWork() {
        when(registrationService.getPendingApplications(3)).thenReturn(List.of());

        scheduler.processPendingApplications();

        verify(creditScoreClient, never()).fetchCreditScore(any(CibilCheckRequest.class));
        verify(registrationService, never()).applyCreditScore(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Happy path: each app's PAN+salary+totalCards=0 is sent and the score is persisted")
    void happyPath_allScored() {
        ApplicationDetailDTO a1 = app(1L, "ABCDE1234F", "1250000.00");
        ApplicationDetailDTO a2 = app(2L, "ZXCVB9876K", "800000.00");
        when(registrationService.getPendingApplications(3)).thenReturn(List.of(a1, a2));
        when(creditScoreClient.fetchCreditScore(any(CibilCheckRequest.class)))
                .thenReturn(Optional.of(720))
                .thenReturn(Optional.of(810));

        scheduler.processPendingApplications();

        ArgumentCaptor<CibilCheckRequest> captor = ArgumentCaptor.forClass(CibilCheckRequest.class);
        verify(creditScoreClient, times(2)).fetchCreditScore(captor.capture());

        CibilCheckRequest first = captor.getAllValues().get(0);
        Assertions.assertThat(first.getPanNo()).isEqualTo("ABCDE1234F");
        Assertions.assertThat(first.getAnnualSalary()).isEqualByComparingTo("1250000.00");
        Assertions.assertThat(first.getTotalCards()).isZero();

        CibilCheckRequest second = captor.getAllValues().get(1);
        Assertions.assertThat(second.getPanNo()).isEqualTo("ZXCVB9876K");
        Assertions.assertThat(second.getAnnualSalary()).isEqualByComparingTo("800000.00");
        Assertions.assertThat(second.getTotalCards()).isZero();

        verify(registrationService).applyCreditScore(1L, 720);
        verify(registrationService).applyCreditScore(2L, 810);
    }

    @Test
    @DisplayName("CIBIL lookup failure: that single app is skipped, others continue")
    void scoreLookupFails_appSkipped() {
        ApplicationDetailDTO a1 = app(1L, "ABCDE1234F", "1250000.00");
        ApplicationDetailDTO a2 = app(2L, "ZXCVB9876K", "800000.00");
        when(registrationService.getPendingApplications(3)).thenReturn(List.of(a1, a2));
        when(creditScoreClient.fetchCreditScore(any(CibilCheckRequest.class)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(810));

        scheduler.processPendingApplications();

        verify(registrationService, never()).applyCreditScore(eq(1L), anyInt());
        verify(registrationService).applyCreditScore(2L, 810);
    }

    @Test
    @DisplayName("Persist failure on one app does not abort the batch")
    void persistFails_otherAppsStillProcessed() {
        ApplicationDetailDTO a1 = app(1L, "ABCDE1234F", "1250000.00");
        ApplicationDetailDTO a2 = app(2L, "ZXCVB9876K", "800000.00");
        when(registrationService.getPendingApplications(3)).thenReturn(List.of(a1, a2));
        when(creditScoreClient.fetchCreditScore(any(CibilCheckRequest.class)))
                .thenReturn(Optional.of(720))
                .thenReturn(Optional.of(810));
        when(registrationService.applyCreditScore(1L, 720))
                .thenThrow(new RuntimeException("DB blip"));

        scheduler.processPendingApplications();

        verify(registrationService, times(1)).applyCreditScore(1L, 720);
        verify(registrationService, times(1)).applyCreditScore(2L, 810);
    }
}
