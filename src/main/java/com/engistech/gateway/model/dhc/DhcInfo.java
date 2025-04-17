package com.engistech.gateway.model.dhc;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Min;
import com.engistech.gateway.model.common.CellularInfo;
import com.engistech.gateway.model.common.GpsData;
import com.engistech.gateway.model.common.Odometer;
import com.engistech.gateway.model.common.Provisioning;
import com.engistech.gateway.model.common.TirePressure;
import com.engistech.gateway.model.common.Windows;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DhcInfo {
    private GpsData gpsData;
    private GpsData lastValidGpsData;
    private CellularInfo cellularInfo;
    private IgnitionEnum ignition;
    private String fuelLevel;
    private TirePressure tirePressure;
    private Odometer odometer;
    private Windows windows;

    @Min(0)
    @DecimalMax("99.99")
    private double batteryVoltage;
    
    private String eventTimestampUTC;
    private List<DcmDtc> dcmDTCList;
    private Provisioning provisioning;

    public DhcInfo() {
        this.gpsData = new GpsData();
        this.lastValidGpsData = new GpsData();
        this.cellularInfo = new CellularInfo();
        this.tirePressure = new TirePressure();
        this.odometer = new Odometer();
        this.windows = new Windows();
        this.dcmDTCList = new ArrayList<>();
        this.provisioning = new Provisioning();
    }
    
    public enum IgnitionEnum {
        RUN("RUN"),
        IG_ON("IG_ON"),
        ACC_ON("ACC_ON"),
        IG_OFF("IG_OFF"),
        UNKNOWN("UNKNOWN");

        private final String value;

        IgnitionEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static IgnitionEnum fromValue(String value) {
            for (IgnitionEnum item : IgnitionEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid Ignition value: " + value);
        }
    }
}