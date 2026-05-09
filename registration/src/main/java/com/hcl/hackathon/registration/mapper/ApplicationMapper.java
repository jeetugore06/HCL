package com.hcl.hackathon.registration.mapper;

import org.springframework.stereotype.Component;

import com.hcl.hackathon.registration.dto.ApplicationDetailDTO;
import com.hcl.hackathon.registration.dto.ApplicationRequestDTO;
import com.hcl.hackathon.registration.dto.ApplicationResponseDTO;
import com.hcl.hackathon.registration.entity.CreditCardApplication;
import com.hcl.hackathon.registration.util.ApplicationStatus;

@Component
public class ApplicationMapper {

    /** Maps Request DTO -> new Entity. */
    public CreditCardApplication toEntity(ApplicationRequestDTO dto) {
        CreditCardApplication entity = new CreditCardApplication();
        entity.setApplicationReference(java.util.UUID.randomUUID().toString());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setAnnualSalary(dto.getAnnualSalary());
        entity.setEmployerName(dto.getEmployerName());
        entity.setEmploymentType(dto.getEmploymentType());
        entity.setDocumentType(dto.getDocumentType());
        entity.setDocumentId(dto.getDocumentId());
        entity.setStatus(ApplicationStatus.SUBMITTED);
        return entity;
    }

    /** Applies the values from a Request DTO onto an existing Entity (used for PUT). */
    public CreditCardApplication updateEntity(CreditCardApplication entity, ApplicationRequestDTO dto) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setAnnualSalary(dto.getAnnualSalary());
        entity.setEmployerName(dto.getEmployerName());
        entity.setEmploymentType(dto.getEmploymentType());
        entity.setDocumentType(dto.getDocumentType());
        entity.setDocumentId(dto.getDocumentId());
        return entity;
    }

    /** Maps Entity -> short Response DTO (used for create / update acknowledgements). */
    public ApplicationResponseDTO toResponseDTO(CreditCardApplication entity) {
        return new ApplicationResponseDTO(
            entity.getApplicationReference(),
            entity.getStatus().name(),
            "Application submitted successfully"
        );
    }

    /** Maps Entity -> full Detail DTO (used for read / list). */
    public ApplicationDetailDTO toDetailDTO(CreditCardApplication entity) {
        return ApplicationDetailDTO.builder()
                .id(entity.getId())
                .applicationReference(entity.getApplicationReference())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .dateOfBirth(entity.getDateOfBirth())
                .annualSalary(entity.getAnnualSalary())
                .employerName(entity.getEmployerName())
                .employmentType(entity.getEmploymentType())
                .documentType(entity.getDocumentType())
                .documentId(entity.getDocumentId())
                .creditScore(entity.getCreditScore())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .decisionReason(entity.getDecisionReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
