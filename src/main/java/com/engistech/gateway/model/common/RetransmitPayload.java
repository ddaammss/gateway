package com.engistech.gateway.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetransmitPayload {
    private CommonHeader header;

    public RetransmitPayload() {
        this.header = new CommonHeader();
    }
}