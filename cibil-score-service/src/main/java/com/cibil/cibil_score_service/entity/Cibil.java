package com.cibil.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_cibil_score"
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "panNo")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cibil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String panNo;

    private Integer score;

    private String status;
}