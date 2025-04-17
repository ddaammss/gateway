package com.engistech.gateway.model.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorInfo {
    private int code;
    private String description;
}