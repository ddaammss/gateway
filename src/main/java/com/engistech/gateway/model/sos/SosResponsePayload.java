package com.engistech.gateway.model.sos;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SosResponsePayload {
    private CommonHeader header;

    public SosResponsePayload() {
        this.header = new CommonHeader();
    }
}