package com.officebuddy.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {

    @Builder.Default
    @Column(name = "is_active")
    private Integer isActive = 1;

    @Column(name = "remarks")
    private String remarks;

    @Builder.Default
    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    @Builder.Default
    @Column(name = "created_by", nullable = false)
    private Long createdBy = 1L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date updatedAt;

    @PrePersist
    protected void baseOnCreate() {
        if (createdAt == null) createdAt = new Date();
        if (updatedAt == null) updatedAt = new Date();
        if (isActive == null) isActive = 1;
        if (isDeleted == null) isDeleted = 0;
        if (createdBy == null) createdBy = 1L;
    }

    @PreUpdate
    protected void baseOnUpdate() {
        updatedAt = new Date();
    }
}
