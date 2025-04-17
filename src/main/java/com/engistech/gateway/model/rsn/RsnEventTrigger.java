package com.engistech.gateway.model.rsn;

import com.engistech.gateway.model.common.EventTriggerType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RsnEventTrigger implements EventTriggerType {

    HU("HU"),
    CENTER_REQUEST("CENTER_REQUEST"),
    OTHER("OTHER");

    private final String value;

    RsnEventTrigger(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static RsnEventTrigger fromValue(String value) {
        for (RsnEventTrigger item : RsnEventTrigger.values()) {
            if (item.value.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid RsnEventTrigger value: " + value);
    }
}