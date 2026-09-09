package com.officebuddy.todo;

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
@Table(name = "todos")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Todo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private UUID goalId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String type;

    private LocalDate dueDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean completed = false;

    private LocalDateTime deletedAt;

}
