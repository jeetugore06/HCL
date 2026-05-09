package com.zbank.customerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    private Long id;

    @Column(name = "customer_reference", nullable = false, columnDefinition = "CHAR(36)")
    private String customerReference;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "annual_salary", nullable = false)
    private java.math.BigDecimal annualSalary;

    @Column(name = "employer_name", length = 120)
    private String employerName;

    @Column(name = "employment_type", nullable = false, length = 20)
    private String employmentType;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(name = "document_id", nullable = false, length = 40)
    private String documentId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDocumentId() {
        return documentId;
    }
}
