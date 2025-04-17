package com.engistech.gateway.model.sos;

import com.engistech.gateway.model.common.EventTriggerType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SosEventTrigger implements EventTriggerType {

    EDSS("EDSS"),
    SOS_BUTTON("SOS_BUTTON"),
    CENTER_REQUEST("CENTER_REQUEST"),
    OTHER("OTHER");

    private final String value;

    SosEventTrigger(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static SosEventTrigger fromValue(String value) {
        for (SosEventTrigger item : SosEventTrigger.values()) {
            if (item.value.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid SosEventTrigger value: " + value);
    }
}