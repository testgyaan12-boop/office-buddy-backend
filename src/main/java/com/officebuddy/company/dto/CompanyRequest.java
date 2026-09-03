package com.officebuddy.company.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.officebuddy.common.FlexibleLocalDateDeserializer;
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
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate startDate;
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
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
