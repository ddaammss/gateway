package com.engistech.gateway.model.provisioning;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProvisioningPayload {
    private CommonHeader header;
    private ProvisioningPayloadBody body;

    public ProvisioningPayload() {
        this.header = new CommonHeader();
        this.body = new ProvisioningPayloadBody();
    }
}