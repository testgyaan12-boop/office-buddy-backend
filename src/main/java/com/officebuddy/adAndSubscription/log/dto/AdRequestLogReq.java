package com.officebuddy.adAndSubscription.log.dto;

import lombok.Data;

@Data
public class AdRequestLogReq {
    private String provider;
    private String placement;
    private String adType;
    private String status;
    private String errorCode;
}
