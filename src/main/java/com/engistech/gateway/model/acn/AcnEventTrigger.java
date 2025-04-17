package com.engistech.gateway.model.acn;

import com.engistech.gateway.model.common.EventTriggerType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AcnEventTrigger implements EventTriggerType {

    GSW("GSW"),
    CENTER_REQUEST("CENTER_REQUEST"),
    OTHER("OTHER");

    private final String value;

    AcnEventTrigger(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static AcnEventTrigger fromValue(String value) {
        for (AcnEventTrigger item : AcnEventTrigger.values()) {
            if (item.value.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid AcnEventTrigger value: " + value);
    }
}