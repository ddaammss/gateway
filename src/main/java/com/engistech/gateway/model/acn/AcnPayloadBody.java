package com.engistech.gateway.model.acn;

import com.engistech.gateway.model.common.VehicleReport;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcnPayloadBody {
    private VehicleReport<AcnEventTrigger> vehicleReport;

    public AcnPayloadBody() {
        this.vehicleReport = new VehicleReport<>(); 
    }
}