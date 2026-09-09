package com.officebuddy.company;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    private String role;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private boolean isCurrent;

    private LocalDateTime deletedAt;
}
