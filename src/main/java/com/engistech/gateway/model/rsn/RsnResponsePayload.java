package com.engistech.gateway.model.rsn;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RsnResponsePayload {
    private CommonHeader header;

    public RsnResponsePayload() {
        this.header = new CommonHeader();
    }
}