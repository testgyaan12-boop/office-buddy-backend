package com.officebuddy.lookup;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lookups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lookupid;

    @Column(name = "lookup_code", nullable = false)
    @JsonProperty("lookup_code")
    private String lookupCode;

    @Column(name = "short_name", nullable = false)
    @JsonProperty("short_name")
    private String shortName;

    @Column(name = "long_name")
    @JsonProperty("long_name")
    private String longName;

    @Column(name = "parent_lookup_id")
    @JsonProperty("parent_lookup_id")
    private Long parentLookupId;

    @Column(name = "sorted_order", nullable = false)
    @JsonProperty("sorted_order")
    private Integer sortedOrder;

    @Column(name = "is_active", nullable = false)
    @JsonProperty("is_active")
    private Boolean isActive;

    @Column(name = "is_deleted", nullable = false)
    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @Column(name = "remarks", length = 150)
    private String remarks;

    @Column(name = "created_at", updatable = false)
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (isActive == null) isActive = true;
        if (isDeleted == null) isDeleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
