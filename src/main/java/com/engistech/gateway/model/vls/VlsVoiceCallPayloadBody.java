package com.engistech.gateway.model.vls;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsVoiceCallPayloadBody {
    private VlsCallSetting callSetting;

    public VlsVoiceCallPayloadBody() {
        this.callSetting = new VlsCallSetting();
    }
}