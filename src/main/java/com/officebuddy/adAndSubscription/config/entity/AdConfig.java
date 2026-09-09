package com.officebuddy.adAndSubscription.config.entity;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ad_config")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "ad_type", nullable = false)
    private String adType;

    @Column(nullable = false)
    private String placement;

    @Column(name = "ad_unit_id", nullable = false)
    private String adUnitId;

    @Column(name = "app_id")
    private String appId;

    @Column(nullable = false)
    private Integer priority;

}
