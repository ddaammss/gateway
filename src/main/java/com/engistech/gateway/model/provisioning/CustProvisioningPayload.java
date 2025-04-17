package com.engistech.gateway.model.provisioning;

import com.engistech.gateway.model.common.CommonHeader;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CustProvisioningPayload {
    private CommonHeader header;
    private CustProvisioningPayloadBody body;

    public CustProvisioningPayload() {
        this.header = new CommonHeader();
        this.body = new CustProvisioningPayloadBody();
    }
}