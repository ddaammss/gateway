package com.engistech.gateway.model.dhc;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DcmDtc {
    private String description;
    private String failureType;
    private boolean testFailed;
    private boolean confirmedDTC;
    private boolean testFailedSinceLastClear;
    private String ssrTimeInformationUTC;
    private double ssrVoltage;
}