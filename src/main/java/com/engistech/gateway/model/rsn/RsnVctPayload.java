package com.engistech.gateway.model.rsn;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RsnVctPayload {
    private CommonHeader header;
    private RsnVctPayloadBody body;

    public RsnVctPayload() {
        this.header = new CommonHeader();
        this.body = new RsnVctPayloadBody();
    }
}