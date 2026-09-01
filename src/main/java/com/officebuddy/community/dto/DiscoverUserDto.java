package com.officebuddy.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoverUserDto {
    private String id;
    private String name;
    private String email;
    private String avatarUrl;
    private String headline;
    private String currentCompany;
    private String skills;
    private boolean online;
    private String friendStatus;
}
