package com.engistech.gateway.model.rsn;

import com.engistech.gateway.model.common.VehicleReport;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RsnPayloadBody {
    private VehicleReport<RsnEventTrigger> vehicleReport;

    public RsnPayloadBody() {
        this.vehicleReport = new VehicleReport<>();
    }
}