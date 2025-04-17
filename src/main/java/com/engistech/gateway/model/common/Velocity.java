package com.engistech.gateway.model.common;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Velocity {

    private UnitEnum unit;

    @Min(-1)
    @Max(512)
    private int value;

    public enum UnitEnum {
        KPH("KPH"),
        MPH("MPH");

        private final String value;

        UnitEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static UnitEnum fromValue(String value) {
            for (UnitEnum item : UnitEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid Unit value: " + value);
        }
    }
}