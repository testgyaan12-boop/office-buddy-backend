package com.officebuddy.goal;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "goals")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Goal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    private String category;

    @Builder.Default
    @Column(nullable = false)
    private String status = "active";

    private LocalDateTime deletedAt;

}
