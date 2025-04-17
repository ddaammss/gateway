package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsStartPayload {
    private CommonHeader header;
    private VlsStartPayloadBody body;

    public VlsStartPayload() {
        this.header = new CommonHeader();
        this.body = new VlsStartPayloadBody();
    }
}