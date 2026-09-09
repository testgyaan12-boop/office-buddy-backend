package com.officebuddy.adAndSubscription.provider.entity;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ad_provider_master", uniqueConstraints = @UniqueConstraint(columnNames = {"provider_name", "platform"}))
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdProvider extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_name", nullable = false)
    private String providerName;

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false)
    private Integer priority;
}
