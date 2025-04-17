package com.engistech.gateway.model.dhc;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DhcPayload {
    private CommonHeader header;
    private DhcPayloadBody body;

    public DhcPayload() {
        this.header = new CommonHeader();
        this.body = new DhcPayloadBody();
    }
}