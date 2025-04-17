package com.engistech.gateway.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorPayload {
    private CommonHeader header;
    private ErrorInfo body;

    public ErrorPayload() {
        this.header = new CommonHeader();
        this.body = new ErrorInfo();
    }
}