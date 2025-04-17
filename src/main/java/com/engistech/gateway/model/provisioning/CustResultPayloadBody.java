package com.engistech.gateway.model.provisioning;

import com.engistech.gateway.model.common.ErrorInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustResultPayloadBody {
    private ErrorInfo error;

    public CustResultPayloadBody() {
        this.error = new ErrorInfo();
    }
}