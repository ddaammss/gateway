package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsResponsePayload {
    private CommonHeader header;

    public VlsResponsePayload() {
        this.header = new CommonHeader();
    }
}