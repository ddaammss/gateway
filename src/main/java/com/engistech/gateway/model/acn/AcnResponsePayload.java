package com.engistech.gateway.model.acn;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcnResponsePayload {
    private CommonHeader header;

    public AcnResponsePayload() {
        this.header = new CommonHeader();
    }
}