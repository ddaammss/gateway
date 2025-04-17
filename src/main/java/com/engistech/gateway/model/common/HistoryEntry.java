package com.engistech.gateway.model.common;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryEntry {
    @Min(1)
    @Max(64)
    private String no;
    
    private Coordinate coordinate;
    private String dateTimeUTC;

    public HistoryEntry() {
        this.coordinate = new Coordinate(); 
    }
}