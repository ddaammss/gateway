package com.engistech.gateway.model.acn;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcnPayload {
    private CommonHeader header;
    private AcnPayloadBody body;

    public AcnPayload() {
        this.header = new CommonHeader();
        this.body = new AcnPayloadBody();
    }
}