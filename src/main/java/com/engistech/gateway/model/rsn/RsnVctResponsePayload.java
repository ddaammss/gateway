package com.engistech.gateway.model.rsn;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RsnVctResponsePayload {
    private CommonHeader header;

    public RsnVctResponsePayload() {
        this.header = new CommonHeader();
    }
}