package com.engistech.gateway.model.dhc;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DhcResponsePayloadBody {
    private DhcInterval interval;

    public DhcResponsePayloadBody() {
        this.interval = new DhcInterval();
    }
}