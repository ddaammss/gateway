package com.engistech.gateway.model.provisioning;

import com.engistech.gateway.model.common.VehicleReport;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustPayloadBody {
    private VehicleReport<CustEventTrigger> vehicleReport;

    public CustPayloadBody() {
        this.vehicleReport = new VehicleReport<>();
    }
}