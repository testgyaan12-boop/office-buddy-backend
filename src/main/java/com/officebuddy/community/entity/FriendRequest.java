package com.officebuddy.community.entity;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "friend_requests")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID senderId;

    @Column(nullable = false)
    private UUID receiverId;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING";

}
