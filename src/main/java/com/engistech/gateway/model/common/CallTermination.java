package com.engistech.gateway.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CallTermination {

    CUSTOMER("CUSTOMER"),
    OTHER("OTHER");

    private final String value;

    CallTermination(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static CallTermination fromValue(String value) {
        for (CallTermination item : CallTermination.values()) {
            if (item.value.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid CallTermination value: " + value);
    }
}