package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsVoiceCallPayload {
    private CommonHeader header;
    private VlsVoiceCallPayloadBody body;

    public VlsVoiceCallPayload() {
        this.header = new CommonHeader();
        this.body = new VlsVoiceCallPayloadBody();
    }
}