package com.officebuddy.lookup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lookups")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Lookup extends BaseEntity {

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

}
