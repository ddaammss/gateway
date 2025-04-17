package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.ErrorInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsVoiceCallResultPayloadBody {
    private ErrorInfo error;

    public VlsVoiceCallResultPayloadBody() {
        this.error = new ErrorInfo();
    }
}