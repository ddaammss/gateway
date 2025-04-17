package com.engistech.gateway.model.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {

    private int resultCode;
    private String resultMessage;
    private T dcmMessage;

    public ApiResponse() {
        this.resultCode = HttpStatus.OK.value();
        this.resultMessage = "";
        this.dcmMessage = null;
    }
}