package com.officebuddy.adAndSubscription.subscription.plan.entity;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
public class Plan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "plan_code", nullable = false, unique = true)
    private String planCode;

    @Column(name = "period")
    private String period;

    @Column(name = "allocated_bytes", nullable = false)
    private Long allocatedBytes;

    @Column(name = "allocated_unit", nullable = false)
    private String allocatedUnit;

    public Plan(String planName, String planCode, String period, Long allocatedBytes, String allocatedUnit) {
        this.planName = planName;
        this.planCode = planCode;
        this.period = period;
        this.allocatedBytes = allocatedBytes;
        this.allocatedUnit = allocatedUnit;
    }
}
