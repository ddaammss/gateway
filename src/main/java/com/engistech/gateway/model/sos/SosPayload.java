package com.engistech.gateway.model.sos;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SosPayload {
    private CommonHeader header;
    private SosPayloadBody body;

    public SosPayload() {
        this.header = new CommonHeader();
        this.body = new SosPayloadBody();
    }
}