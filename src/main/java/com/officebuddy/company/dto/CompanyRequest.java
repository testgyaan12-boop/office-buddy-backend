package com.officebuddy.company.dto;

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
public class CompanyRequest {
    private String name;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;

    @JsonProperty("isCurrent")
    public boolean isCurrent() { return isCurrent; }

    @JsonProperty("isCurrent")
    @JsonAlias({"current", "is_current"})
    public void setCurrent(boolean isCurrent) { this.isCurrent = isCurrent; }

    // Jackson fallback for "isCurrent" setter name
    @JsonProperty("isCurrent")
    public void setIsCurrent(boolean isCurrent) { this.isCurrent = isCurrent; }
}
