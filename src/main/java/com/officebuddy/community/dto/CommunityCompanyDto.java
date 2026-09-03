package com.officebuddy.community.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityCompanyDto {
    private String name;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    @JsonProperty("isCurrent")
    @JsonAlias({"current", "is_current"})
    private Boolean isCurrent;
}
