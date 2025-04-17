package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsVctPayload {
    private CommonHeader header;

    public VlsVctPayload() {
        this.header = new CommonHeader();
    }
}