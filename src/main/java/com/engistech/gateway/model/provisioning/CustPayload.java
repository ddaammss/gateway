package com.engistech.gateway.model.provisioning;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustPayload {
    private CommonHeader header;
    private CustPayloadBody body;

    public CustPayload() {
        this.header = new CommonHeader();
        this.body = new CustPayloadBody();
    }
}