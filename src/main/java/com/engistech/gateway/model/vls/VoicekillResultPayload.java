package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoicekillResultPayload {
    private CommonHeader header;

    public VoicekillResultPayload() {
        this.header = new CommonHeader();
    }
}