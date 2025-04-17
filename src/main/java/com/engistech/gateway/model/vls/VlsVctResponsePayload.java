package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsVctResponsePayload {
    private CommonHeader header;

    public VlsVctResponsePayload() {
        this.header = new CommonHeader();
    }
}