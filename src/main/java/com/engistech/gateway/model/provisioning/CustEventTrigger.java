package com.engistech.gateway.model.provisioning;

import com.engistech.gateway.model.common.EventTriggerType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CustEventTrigger implements EventTriggerType {

    IG_ON("IG_ON"),
    TELEMA_ON("TELEMA_ON"),
    OTHER("OTHER");

    private final String value;

    CustEventTrigger(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static CustEventTrigger fromValue(String value) {
        for (CustEventTrigger item : CustEventTrigger.values()) {
            if (item.value.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid CustEventTrigger value: " + value);
    }
}