package com.engistech.gateway.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TirePressure {
    private String unit;
    private TireInfo tire1;
    private TireInfo tire2;
    private TireInfo tire3;
    private TireInfo tire4;
    private TireInfo tire5;

    public TirePressure() {
        this.tire1 = new TireInfo();
        this.tire2 = new TireInfo();
        this.tire3 = new TireInfo();
        this.tire4 = new TireInfo();
        this.tire5 = new TireInfo();
    }
}