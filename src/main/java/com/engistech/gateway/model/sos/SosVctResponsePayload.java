package com.engistech.gateway.model.sos;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SosVctResponsePayload {
    private CommonHeader header;

    public SosVctResponsePayload() {
        this.header = new CommonHeader();
    }
}