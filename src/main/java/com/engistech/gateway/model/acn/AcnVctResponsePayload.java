package com.engistech.gateway.model.acn;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcnVctResponsePayload {
    private CommonHeader header;

    public AcnVctResponsePayload() {
        this.header = new CommonHeader();
    }
}