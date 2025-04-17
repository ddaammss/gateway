package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.VehicleReport;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsPayloadBody {
    private VehicleReport<VlsEventTrigger> vehicleReport;

    public VlsPayloadBody() {
        this.vehicleReport = new VehicleReport<>();
    }
}