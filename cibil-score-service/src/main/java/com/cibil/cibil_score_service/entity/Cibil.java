package com.cibil.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_cibil_score")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cibil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer appId;

    private Integer score;

    private String status;
}