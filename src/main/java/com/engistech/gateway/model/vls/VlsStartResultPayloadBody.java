package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.ErrorInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsStartResultPayloadBody {
    private ErrorInfo error;

    public VlsStartResultPayloadBody() {
        this.error = new ErrorInfo();
    }
}