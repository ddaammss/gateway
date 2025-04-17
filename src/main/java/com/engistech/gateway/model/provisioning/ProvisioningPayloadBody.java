package com.engistech.gateway.model.provisioning;

import com.engistech.gateway.model.common.Provisioning;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProvisioningPayloadBody {
    private Provisioning provisioning;

    public ProvisioningPayloadBody() {
        this.provisioning = new Provisioning();
    }
}