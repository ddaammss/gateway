package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoicekillPayload {
    private CommonHeader header;

    public VoicekillPayload() {
        this.header = new CommonHeader();
    }
}