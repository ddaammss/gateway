package com.engistech.gateway.model.rsn;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RsnPayload {
    private CommonHeader header;
    private RsnPayloadBody body;

    public RsnPayload() {
        this.header = new CommonHeader();
        this.body = new RsnPayloadBody();
    }
}