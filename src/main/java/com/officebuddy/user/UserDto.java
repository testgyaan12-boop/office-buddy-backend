package com.officebuddy.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String avatarUrl;
    private String headline;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String currentCompany;
    private String salary;
    private String expectedSalary;
    private String skills;
    private String address;
    private String bloodGroup;
    private String linkedInUrl;
    private String portfolioUrl;
    private String panNumber;
    private String aadhaarNumber;
    private String uanNumber;
    private String pfNumber;
    private String bankAccountNumber;
    private String ifscCode;
    private String emergencyContact;
}
