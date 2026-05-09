package com.hcl.hackathon.registration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.hcl.hackathon.registration.dto.ApplicationDetailDTO;
import com.hcl.hackathon.registration.dto.ApplicationRequestDTO;
import com.hcl.hackathon.registration.dto.ApplicationResponseDTO;
import com.hcl.hackathon.registration.entity.CreditCardApplication;
import com.hcl.hackathon.registration.exception.ResourceNotFoundException;
import com.hcl.hackathon.registration.mapper.ApplicationMapper;
import com.hcl.hackathon.registration.repo.ApplicationRepository;
import com.hcl.hackathon.registration.util.ApplicationStatus;

/**
 * Service-layer unit tests for the credit-card registration flow.
 * Pure JUnit 5 + Mockito; no Spring context boot.
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private ApplicationRepository repository;

    @Mock
    private ApplicationMapper mapper;

    @InjectMocks
    private RegistrationService service;

    private ApplicationRequestDTO request;
    private CreditCardApplication entity;

    @BeforeEach
    void setUp() {
        request = ApplicationRequestDTO.builder()
                .firstName("Amit")
                .lastName("Rakhaiya")
                .email("amit@example.com")
                .phone("9999999999")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .annualSalary(new BigDecimal("125000.00"))
                .employerName("Acme Corp")
                .employmentType("FULL_TIME")
                .documentType("PAN")
                .documentId("DOC-1")
                .build();

        entity = CreditCardApplication.builder()
                .id(1L)
                .applicationReference("REF-1")
                .firstName("Amit")
                .lastName("Rakhaiya")
                .email("amit@example.com")
                .phone("9999999999")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .annualSalary(new BigDecimal("125000.00"))
                .employerName("Acme Corp")
                .employmentType("FULL_TIME")
                .documentType("PAN")
                .documentId("DOC-1")
                .status(ApplicationStatus.SUBMITTED)
                .build();
    }

    // ---------- CREATE ----------

    @Test
    @DisplayName("processApplication: persists entity and returns ack response")
    void processApplication_success() {
        ApplicationResponseDTO ack = ApplicationResponseDTO.builder()
                .applicationReference("REF-1")
                .status("SUBMITTED")
                .message("Application submitted successfully")
                .build();

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDTO(entity)).thenReturn(ack);

        ApplicationResponseDTO result = service.processApplication(request);

        assertThat(result).isSameAs(ack);
        ArgumentCaptor<CreditCardApplication> captor = ArgumentCaptor.forClass(CreditCardApplication.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getApplicationReference()).isEqualTo("REF-1");
    }

    @Test
    @DisplayName("processApplication: null payload throws IllegalArgumentException")
    void processApplication_null() {
        assertThatThrownBy(() -> service.processApplication(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
        verify(repository, never()).save(any());
    }

    // ---------- READ ALL ----------

    @Test
    @DisplayName("getAllApplications: maps every entity to a detail DTO")
    void getAllApplications_returnsList() {
        ApplicationDetailDTO detail = ApplicationDetailDTO.builder().id(1L).build();
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDetailDTO(entity)).thenReturn(detail);

        List<ApplicationDetailDTO> result = service.getAllApplications();

        assertThat(result).hasSize(1).containsExactly(detail);
        verify(mapper, times(1)).toDetailDTO(entity);
    }

    // ---------- READ BY ID ----------

    @Test
    @DisplayName("getApplicationById: returns detail DTO when found")
    void getApplicationById_found() {
        ApplicationDetailDTO detail = ApplicationDetailDTO.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDetailDTO(entity)).thenReturn(detail);

        ApplicationDetailDTO result = service.getApplicationById(1L);

        assertThat(result).isSameAs(detail);
    }

    @Test
    @DisplayName("getApplicationById: throws ResourceNotFoundException when missing")
    void getApplicationById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getApplicationById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------- READ BY REFERENCE ----------

    @Test
    @DisplayName("getApplicationByReference: returns detail DTO when found")
    void getApplicationByReference_found() {
        ApplicationDetailDTO detail = ApplicationDetailDTO.builder().applicationReference("REF-1").build();
        when(repository.findByApplicationReference("REF-1")).thenReturn(Optional.of(entity));
        when(mapper.toDetailDTO(entity)).thenReturn(detail);

        ApplicationDetailDTO result = service.getApplicationByReference("REF-1");

        assertThat(result.getApplicationReference()).isEqualTo("REF-1");
    }

    @Test
    @DisplayName("getApplicationByReference: throws ResourceNotFoundException when missing")
    void getApplicationByReference_notFound() {
        when(repository.findByApplicationReference("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getApplicationByReference("MISSING"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("MISSING");
    }

    // ---------- NEXT PENDING ----------

    @Test
    @DisplayName("getNextPendingApplication: returns oldest SUBMITTED record when present")
    void getNextPendingApplication_present() {
        ApplicationDetailDTO detail = ApplicationDetailDTO.builder()
                .id(1L).applicationReference("REF-1").status("SUBMITTED").build();
        when(repository.findFirstByStatusOrderByCreatedAtAsc(ApplicationStatus.SUBMITTED))
                .thenReturn(Optional.of(entity));
        when(mapper.toDetailDTO(entity)).thenReturn(detail);

        Optional<ApplicationDetailDTO> result = service.getNextPendingApplication();

        assertThat(result).isPresent().get().isEqualTo(detail);
    }

    @Test
    @DisplayName("getNextPendingApplication: returns empty Optional when queue is empty")
    void getNextPendingApplication_empty() {
        when(repository.findFirstByStatusOrderByCreatedAtAsc(ApplicationStatus.SUBMITTED))
                .thenReturn(Optional.empty());

        Optional<ApplicationDetailDTO> result = service.getNextPendingApplication();

        assertThat(result).isEmpty();
        verify(mapper, never()).toDetailDTO(any());
    }

    // ---------- UPDATE ----------

    @Test
    @DisplayName("updateApplication: applies request DTO and returns updated detail")
    void updateApplication_success() {
        ApplicationDetailDTO detail = ApplicationDetailDTO.builder().id(1L).email("amit@example.com").build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.updateEntity(entity, request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDetailDTO(entity)).thenReturn(detail);

        ApplicationDetailDTO result = service.updateApplication(1L, request);

        assertThat(result).isSameAs(detail);
        verify(mapper).updateEntity(entity, request);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("updateApplication: null payload throws IllegalArgumentException")
    void updateApplication_nullPayload() {
        assertThatThrownBy(() -> service.updateApplication(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("updateApplication: missing entity throws ResourceNotFoundException")
    void updateApplication_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateApplication(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    // ---------- BATCH FETCH (scheduler) ----------

    @Test
    @DisplayName("getPendingApplications: returns mapped batch of unscored SUBMITTED apps")
    void getPendingApplications_success() {
        ApplicationDetailDTO detail = ApplicationDetailDTO.builder().id(1L).build();
        when(repository.findByStatusAndCreditScoreIsNullOrderByCreatedAtAsc(
                eq(ApplicationStatus.SUBMITTED), eq(PageRequest.of(0, 5))))
                .thenReturn(List.of(entity));
        when(mapper.toDetailDTO(entity)).thenReturn(detail);

        List<ApplicationDetailDTO> result = service.getPendingApplications(5);

        assertThat(result).containsExactly(detail);
    }

    @Test
    @DisplayName("getPendingApplications: rejects non-positive batch size")
    void getPendingApplications_invalidBatch() {
        assertThatThrownBy(() -> service.getPendingApplications(0))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never())
                .findByStatusAndCreditScoreIsNullOrderByCreatedAtAsc(any(), any());
    }

    // ---------- APPLY CREDIT SCORE (scheduler) ----------

    @Test
    @DisplayName("applyCreditScore: persists score on the entity")
    void applyCreditScore_success() {
        ApplicationDetailDTO detail = ApplicationDetailDTO.builder().id(1L).creditScore(720).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDetailDTO(entity)).thenReturn(detail);

        ApplicationDetailDTO result = service.applyCreditScore(1L, 720);

        ArgumentCaptor<CreditCardApplication> captor = ArgumentCaptor.forClass(CreditCardApplication.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCreditScore()).isEqualTo(720);
        assertThat(result).isSameAs(detail);
    }

    @Test
    @DisplayName("applyCreditScore: missing entity throws ResourceNotFoundException")
    void applyCreditScore_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyCreditScore(99L, 700))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    // ---------- DELETE ----------

    @Test
    @DisplayName("deleteApplication: removes existing record")
    void deleteApplication_success() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteApplication(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteApplication: missing entity throws ResourceNotFoundException")
    void deleteApplication_notFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteApplication(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
