package com.engistech.gateway.model.acn;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcnVctPayload {
    private CommonHeader header;
    private AcnVctPayloadBody body;

    public AcnVctPayload() {
        this.header = new CommonHeader();
        this.body = new AcnVctPayloadBody();
    }
}