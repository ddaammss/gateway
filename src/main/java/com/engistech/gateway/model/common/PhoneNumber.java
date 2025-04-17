package com.engistech.gateway.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneNumber {
    private ServiceEnum service;
    private TypeEnum type;
    private String value;

    public PhoneNumber() {
    }

    public PhoneNumber(ServiceEnum service, TypeEnum type, String value) {
        this.service = service;
        this.type = type;
        this.value = value;
    }

    public enum ServiceEnum {
        ACN("ACN"),
        SOS("SOS"),
        RSN("RSN"),
        VLS("VLS"),
        INBOUND("INBOUND");

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

    public enum TypeEnum {
        PRIMARY("PRIMARY"),
        SECONDARY("SECONDARY"),
        TOYOTA_UNKNOWN("TOYOTA_UNKNOWN"),
        LEXUS("LEXUS"),
        THIRD("THIRD"),
        FOURTH("FOURTH"),
        FIFTH("FIFTH"),
        SIXTH("SIXTH"),
        SEVENTH("SEVENTH"),
        EIGHTH("EIGHTH"),
        NINTH("NINTH"),
        TENTH("TENTH");

        private final String value;

        TypeEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static TypeEnum fromValue(String value) {
            for (TypeEnum item : TypeEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid Type value: " + value);
        }
    }
}