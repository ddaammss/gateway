package com.engistech.gateway.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceFlag {
    private ServiceEnum service;
    private FlagValueEnum flagValue;

    public ServiceFlag() {
    }

    public ServiceFlag(ServiceEnum service, FlagValueEnum flagValue) {
        this.service = service;
        this.flagValue = flagValue;
    }

    public enum ServiceEnum {
        ACN("ACN"),
        SOS("SOS"),
        RSN("RSN"),
        VLS("VLS"),
        DHC("DHC");

        private final String value;

        ServiceEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static ServiceEnum fromValue(String value) {
            for (ServiceEnum item : ServiceEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid Service value: " + value);
        }
    }

    public enum FlagValueEnum {
        OFF("OFF"),
        ON("ON");

        private final String value;

        FlagValueEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static FlagValueEnum fromValue(String value) {
            for (FlagValueEnum item : FlagValueEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid FlagValue value: " + value);
        }

        public static FlagValueEnum fromInteger(Integer flag) {
            if (flag == null) {
                throw new IllegalArgumentException("Flag value cannot be null");
            }
            switch (flag) {
                case 0:
                    return OFF;
                case 1:
                    return ON;
                default:
                    throw new IllegalArgumentException("Invalid flag value: " + flag);
            }
        }
    }
}