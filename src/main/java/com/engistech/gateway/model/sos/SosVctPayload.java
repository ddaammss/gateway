package com.engistech.gateway.model.sos;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SosVctPayload {
    private CommonHeader header;
    private SosVctPayloadBody body;

    public SosVctPayload() {
        this.header = new CommonHeader();
        this.body = new SosVctPayloadBody();
    }
}