package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsStopPayload {
    private CommonHeader header;

    public VlsStopPayload() {
        this.header = new CommonHeader();
    }
}