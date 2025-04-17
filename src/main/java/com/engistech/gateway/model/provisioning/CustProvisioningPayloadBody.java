package com.engistech.gateway.model.provisioning;

import com.engistech.gateway.model.common.Provisioning;
import com.engistech.gateway.model.common.ReportSetting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustProvisioningPayloadBody {    
    private ReportSetting reportSetting;
    private Provisioning provisioning;

    public CustProvisioningPayloadBody() {
        this.reportSetting = new ReportSetting();
        this.provisioning = new Provisioning();
    }
}