package com.engistech.gateway.model.sos;

import com.engistech.gateway.model.common.VehicleReport;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SosPayloadBody {
    private VehicleReport<SosEventTrigger> vehicleReport;

    public SosPayloadBody() {
        this.vehicleReport = new VehicleReport<>();
    }
}