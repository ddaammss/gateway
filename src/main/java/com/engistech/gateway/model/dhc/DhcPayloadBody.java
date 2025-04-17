package com.engistech.gateway.model.dhc;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DhcPayloadBody {
    private DhcInfo dhc;

    public DhcPayloadBody() {
        this.dhc = new DhcInfo();
    }
}