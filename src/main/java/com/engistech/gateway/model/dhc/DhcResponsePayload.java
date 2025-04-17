package com.engistech.gateway.model.dhc;

import com.engistech.gateway.model.common.CommonHeader;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DhcResponsePayload {
    private CommonHeader header;
    private DhcResponsePayloadBody body;

    public DhcResponsePayload() {
        this.header = new CommonHeader();
        this.body = new DhcResponsePayloadBody();
    }
}