package com.engistech.gateway.model.common;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class VehicleReport<T extends EventTriggerType> {

    private FrontAirbagEnum frontAirbag;
    private SideAirbagEnum sideAirbag;
    private MultipleImpactEnum multipleImpact;

    @Min(1)
    @Max(255)
    private Integer numberOfOccupants; //schemaVersion 01.05.00 추가

    private RearImpactEnum rearImpact;
    private SideImpactSensorEnum sideImpactSensor;
    private RolloverEnum rollover;
    private String deltaVRangeLimit;
    private Impact firstImpact;
    private Impact secondImpact;
    private BubInUseEnum bubInUse;
    private IgnitionEnum ignition;
    private String fuelType;
    private String fuelLevel;
    private UnderRepairEnum underRepair;
    private SeatStatus driverSeat;
    private SeatStatus passengerSeat;
    private SeatStatus centerRearSeat;
    private SeatStatus leftRearSeat;
    private SeatStatus rightRearSeat;
    private SeatStatus centerThirdRowSeat;
    private SeatStatus leftThirdRowSeat;
    private SeatStatus rightThirdRowSeat;
    private GpsData gpsData;
    private GpsData lastValidGpsData;
    private CellularInfo cellularInfo;
    private String eventTimestampUTC;
    private T eventTrigger;
    private VehiclePositionHistory history;

    public VehicleReport() {
        this.firstImpact = new Impact();
        this.secondImpact = new Impact();
        this.driverSeat = new SeatStatus();
        this.passengerSeat = new SeatStatus();
        this.centerRearSeat = new SeatStatus();
        this.leftRearSeat = new SeatStatus();
        this.rightRearSeat = new SeatStatus();
        this.centerThirdRowSeat = new SeatStatus();
        this.leftThirdRowSeat = new SeatStatus();
        this.rightThirdRowSeat = new SeatStatus();
        this.gpsData = new GpsData();
        this.lastValidGpsData = new GpsData();
        this.cellularInfo = new CellularInfo();
        this.history = new VehiclePositionHistory();
    }

    public enum FrontAirbagEnum {
        OFF("OFF"),
        ON("ON"),
        UNKNOWN("UNKNOWN");

        private final String value;

        FrontAirbagEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static FrontAirbagEnum fromValue(String value) {
            for (FrontAirbagEnum item : FrontAirbagEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid FrontAirbag value: " + value);
        }
    }

    public enum SideAirbagEnum {
        OFF("OFF"),
        ON("ON"),
        UNKNOWN("UNKNOWN");

        private final String value;

        SideAirbagEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static SideAirbagEnum fromValue(String value) {
            for (SideAirbagEnum item : SideAirbagEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid SideAirbag value: " + value);
        }
    }

    public enum MultipleImpactEnum {
        OFF("OFF"),
        ON("ON"),
        UNKNOWN("UNKNOWN");

        private final String value;

        MultipleImpactEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static MultipleImpactEnum fromValue(String value) {
            for (MultipleImpactEnum item : MultipleImpactEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid MultipleImpact value: " + value);
        }
    }

    public enum RearImpactEnum {
        OFF("OFF"),
        ON("ON"),
        UNKNOWN("UNKNOWN");

        private final String value;

        RearImpactEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static RearImpactEnum fromValue(String value) {
            for (RearImpactEnum item : RearImpactEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid RearImpact value: " + value);
        }
    }

    public enum SideImpactSensorEnum {
        LEFT("LEFT"),
        RIGHT("RIGHT"),
        OFF("OFF"),
        UNKNOWN("UNKNOWN");

        private final String value;

        SideImpactSensorEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static SideImpactSensorEnum fromValue(String value) {
            for (SideImpactSensorEnum item : SideImpactSensorEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid SideImpactSensor value: " + value);
        }
    }

    public enum RolloverEnum {
        OFF("OFF"),
        ON("ON"),
        UNKNOWN("UNKNOWN");

        private final String value;

        RolloverEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static RolloverEnum fromValue(String value) {
            for (RolloverEnum item : RolloverEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid Rollover value: " + value);
        }
    }

    public enum BubInUseEnum {
        YES("YES"),
        NO("NO"),
        UNKNOWN("UNKNOWN");

        private final String value;

        BubInUseEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static BubInUseEnum fromValue(String value) {
            for (BubInUseEnum item : BubInUseEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid BubInUse value: " + value);
        }
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

    public enum UnderRepairEnum {
        OFF("OFF"),
        ON("ON"),
        UNKNOWN("UNKNOWN");

        private final String value;

        UnderRepairEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static UnderRepairEnum fromValue(String value) {
            for (UnderRepairEnum item : UnderRepairEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid UnderRepair value: " + value);
        }
    }
}