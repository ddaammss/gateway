package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.ErrorInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsStopResultPayloadBody {
    private ErrorInfo error;

    public VlsStopResultPayloadBody() {
        this.error = new ErrorInfo();
    }
}