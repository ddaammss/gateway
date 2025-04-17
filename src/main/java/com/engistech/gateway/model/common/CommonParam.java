package com.engistech.gateway.model.common;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CommonParam {
    private String deviceId;
    private String ecuId;
    private String appId;
    private String service;
    private Boolean simFlag;
    private Map<String, Object> additionalData; // 동적 데이터 처리

    @JsonAnyGetter
    public Map<String, Object> getAdditionalData() {
        if (additionalData == null) {
            additionalData = new HashMap<>();
        }
        return additionalData;
    }

    @JsonAnySetter
    public void setAdditionalData(String key, Object value) {
        if (additionalData == null) {
            additionalData = new HashMap<>();
        }
        additionalData.put(key, value);
    }
}