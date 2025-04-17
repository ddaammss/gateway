package com.engistech.gateway.model.vls;

import com.engistech.gateway.model.common.CommonHeader;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class VlsStartResultPayload {
    private CommonHeader header;
    private VlsStartResultPayloadBody body;

    public VlsStartResultPayload() {
        this.header = new CommonHeader();
        this.body = new VlsStartResultPayloadBody();
    }
}