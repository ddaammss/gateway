package com.engistech.gateway.model.provisioning;

import com.engistech.gateway.model.common.ErrorInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProvisioningResultPayloadBody {
    private ErrorInfo error;

    public ProvisioningResultPayloadBody() {
        this.error = new ErrorInfo();
    }
}