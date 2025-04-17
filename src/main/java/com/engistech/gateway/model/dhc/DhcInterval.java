package com.engistech.gateway.model.dhc;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DhcInterval {
    private String unit;

    @Min(1)
    @Max(180)
    private int value;
}