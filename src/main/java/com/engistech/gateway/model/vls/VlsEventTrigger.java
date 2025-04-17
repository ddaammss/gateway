package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.EventTriggerType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VlsEventTrigger implements EventTriggerType {

    IG_ON("IG_ON"),
    IG_OFF("IG_OFF"),
    PERIODIC("PERIODIC"),
    CENTER_REQUEST("CENTER_REQUEST"),
    BATTERY_DISCONNECT("BATTERY_DISCONNECT"),
    OTHER("OTHER");

    private final String value;

    VlsEventTrigger(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static VlsEventTrigger fromValue(String value) {
        for (VlsEventTrigger item : VlsEventTrigger.values()) {
            if (item.value.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid VlsEventTrigger value: " + value);
    }
}