package com.engistech.gateway.model.common;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GpsData {
    @Min(-1)
    @Max(359)
    private String heading;

    private String system;
    private Coordinate coordinate;
    private Velocity velocity;
    private String dateTimeUTC;

    @Min(0)
    @Max(999)
    private String accurate;

    public GpsData() {
        this.coordinate = new Coordinate();
        this.velocity = new Velocity();
    }
}