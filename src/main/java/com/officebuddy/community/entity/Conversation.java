package com.officebuddy.community.entity;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userAId;

    @Column(nullable = false)
    private UUID userBId;

    @Column(columnDefinition = "TEXT")
    private String lastMessage;

    private LocalDateTime lastMessageAt;

}
