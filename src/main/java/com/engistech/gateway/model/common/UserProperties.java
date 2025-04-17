package com.engistech.gateway.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProperties {
    private String correlationId;
    private String schemaVersion;
    private String sessionId;
    private String userAgent;
    private Long sequenceId;
}