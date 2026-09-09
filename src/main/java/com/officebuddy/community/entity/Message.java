package com.officebuddy.community.entity;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID conversationId;

    @Column(nullable = false)
    private UUID senderId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private String type = "TEXT";

    private String fileUrl;

    private LocalDateTime readAt;
}
