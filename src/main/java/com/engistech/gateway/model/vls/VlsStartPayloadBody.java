package com.engistech.gateway.model.vls;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsStartPayloadBody {
    private VlsStartReportSetting reportSetting;

    public VlsStartPayloadBody() {
        this.reportSetting = new VlsStartReportSetting();
    }
}