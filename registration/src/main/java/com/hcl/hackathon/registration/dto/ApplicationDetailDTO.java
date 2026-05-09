package com.hcl.hackathon.registration.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full representation of a credit-card application — used for read / list / update responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailDTO {

    private Long id;
    private String applicationReference;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private BigDecimal annualSalary;
    private String employerName;
    private String employmentType;
    private String documentType;
    private String documentId;
    private Integer creditScore;
    private String status;
    private String decisionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
