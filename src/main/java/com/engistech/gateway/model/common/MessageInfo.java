package com.engistech.gateway.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageInfo {
    
    private TypeEnum type;
    private ServiceEnum service;
    private OperationEnum operation;

    public enum TypeEnum {
        REQUEST("REQUEST"),
        REPORT("REPORT"),
        ACK("ACK"),
        NACK("NACK"),
        FAILED("FAILED");

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

    public enum ServiceEnum {
        ACN("ACN"),
        SOS("SOS"),
        RSN("RSN"),
        VLS("VLS"),
        CUST("CUST"),
        DHC("DHC"),
        PROV("PROV"),
        ALL("ALL");

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

    public enum OperationEnum {
        NOTIFICATION("NOTIFICATION"),
        RETRANSMIT("RETRANSMIT"),
        DHC("DHC"),
        CUST("CUST"),
        CONFIRM_TERMINATION("CONFIRM_TERMINATION"),
        START("START"),
        STOP("STOP"),
        VOICE_CALL("VOICE_CALL"),
        PROVISIONING("PROVISIONING"),
        VOICE_KILL("VOICE_KILL"),
        REPORT("REPORT");

        private final String value;

        OperationEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static OperationEnum fromValue(String value) {
            for (OperationEnum item : OperationEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid Operation value: " + value);
        }
    }
}