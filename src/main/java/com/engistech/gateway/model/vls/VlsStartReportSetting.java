package com.engistech.gateway.model.vls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsStartReportSetting {

    private PriorityEnum priority;
    private ActivateTimeLimitEnum activateTimeLimit;
    private TimeLimit timeLimit;
    private IgnitionONReportEnum ignitionONReport;
    private IgnitionOFFReportEnum ignitionOFFReport;
    private ActivateTimeIntervalEnum activateTimeInterval;
    private VlsStartInterval interval;
    private HistoryReportEnum historyReport;

    public VlsStartReportSetting() {
        this.timeLimit = new TimeLimit();
        this.interval = new VlsStartInterval();
    }

    public enum PriorityEnum {

        EMERGENCY("EMERGENCY"),
        NON_EMERGENCY("NON_EMERGENCY");

        private final String value;

        PriorityEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static PriorityEnum fromValue(String value) {
            for (PriorityEnum item : PriorityEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid Priority value: " + value);
        }
    }

    public enum ActivateTimeLimitEnum {
        
        OFF("OFF"),
        ON("ON");

        private final String value;

        ActivateTimeLimitEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static ActivateTimeLimitEnum fromValue(String value) {
            for (ActivateTimeLimitEnum item : ActivateTimeLimitEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid ActivateTimeLimit value: " + value);
        }
    }

    public enum IgnitionONReportEnum {
        
        OFF("OFF"),
        ON("ON");

        private final String value;

        IgnitionONReportEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static IgnitionONReportEnum fromValue(String value) {
            for (IgnitionONReportEnum item : IgnitionONReportEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid IgnitionONReport value: " + value);
        }
    }

    public enum IgnitionOFFReportEnum {
        
        OFF("OFF"),
        ON("ON");

        private final String value;

        IgnitionOFFReportEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static IgnitionOFFReportEnum fromValue(String value) {
            for (IgnitionOFFReportEnum item : IgnitionOFFReportEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid IgnitionOFFReport value: " + value);
        }
    }

    public enum ActivateTimeIntervalEnum {
        
        OFF("OFF"),
        ON("ON");

        private final String value;

        ActivateTimeIntervalEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static ActivateTimeIntervalEnum fromValue(String value) {
            for (ActivateTimeIntervalEnum item : ActivateTimeIntervalEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid ActivateTimeInterval value: " + value);
        }
    }

    public enum HistoryReportEnum {

        YES("YES"),
        NO("NO");

        private final String value;

        HistoryReportEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static HistoryReportEnum fromValue(String value) {
            for (HistoryReportEnum item : HistoryReportEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid HistoryReport value: " + value);
        }
    }
}