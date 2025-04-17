package com.engistech.gateway.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeatStatus {

    private OccupantEnum occupant;
    private BuckleEnum buckle;

    public enum OccupantEnum {
        OFF("OFF"),
        ON("ON"),
        UNKNOWN("UNKNOWN");

        private final String value;

        OccupantEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static OccupantEnum fromValue(String value) {
            for (OccupantEnum item : OccupantEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid Occupant value: " + value);
        }
    }

    public enum BuckleEnum {
        OFF("OFF"),
        ON("ON"),
        UNKNOWN("UNKNOWN");

        private final String value;

        BuckleEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static BuckleEnum fromValue(String value) {
            for (BuckleEnum item : BuckleEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid Buckle value: " + value);
        }
    }
}