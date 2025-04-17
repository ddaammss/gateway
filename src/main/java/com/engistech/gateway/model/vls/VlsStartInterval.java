package com.engistech.gateway.model.vls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsStartInterval {
    
    private UnitEnum unit;
    private int value;

    public enum UnitEnum {
        SEC("SEC"),
        MIN("MIN");

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