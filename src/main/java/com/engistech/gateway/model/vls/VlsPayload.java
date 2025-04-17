package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsPayload {
    private CommonHeader header;
    private VlsPayloadBody body;

    public VlsPayload() {
        this.header = new CommonHeader();
        this.body = new VlsPayloadBody();
    }
}