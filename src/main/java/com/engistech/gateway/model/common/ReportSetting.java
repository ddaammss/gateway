package com.engistech.gateway.model.common;

import com.engistech.gateway.model.dhc.DhcInterval;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportSetting {
    private DhcInterval interval;

    public ReportSetting() {
        this.interval = new DhcInterval();
    }
}