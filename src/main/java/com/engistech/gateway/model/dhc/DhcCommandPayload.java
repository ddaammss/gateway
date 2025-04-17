package com.engistech.gateway.model.dhc;

import com.engistech.gateway.model.common.CommonHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DhcCommandPayload {
    private CommonHeader header;

    public DhcCommandPayload() {
        this.header = new CommonHeader();
    }
}