package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsStopResultPayload {
    private CommonHeader header;
    private VlsStopResultPayloadBody body;

    public VlsStopResultPayload() {
        this.header = new CommonHeader();
        this.body = new VlsStopResultPayloadBody();
    }
}