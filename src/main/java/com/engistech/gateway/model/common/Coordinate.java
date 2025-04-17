package com.engistech.gateway.model.common;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Coordinate {

    private String unit;
        
    @Min(-324000000)
    @Max(324000000)
    private String lat;

    @Min(-648000000)
    @Max(648000000)
    private String lon;
}