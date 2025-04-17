package com.engistech.gateway.model.provisioning;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustResultPayload {
    private CommonHeader header;
    private CustResultPayloadBody body;

    public CustResultPayload() {
        this.header = new CommonHeader();
        this.body = new CustResultPayloadBody();
    }
}