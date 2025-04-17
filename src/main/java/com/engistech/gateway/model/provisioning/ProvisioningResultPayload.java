package com.engistech.gateway.model.provisioning;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProvisioningResultPayload {
    private CommonHeader header;
    private ProvisioningResultPayloadBody body;

    public ProvisioningResultPayload() {
        this.header = new CommonHeader();
        this.body = new ProvisioningResultPayloadBody();
    }
}