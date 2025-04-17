package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsVoiceCallResultPayload {
    private CommonHeader header;
    private VlsVoiceCallResultPayloadBody body;

    public VlsVoiceCallResultPayload() {
        this.header = new CommonHeader();
        this.body = new VlsVoiceCallResultPayloadBody();
    }
}