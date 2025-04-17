package com.engistech.gateway.model.vls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VlsCallSetting {

    private HmiEnum hmi;

    public enum HmiEnum {
        OFF("OFF"),
        ON("ON");

        private final String value;

        HmiEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static HmiEnum fromValue(String value) {
            for (HmiEnum item : HmiEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid Hmi value: " + value);
        }
    }
}
