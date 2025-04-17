package com.engistech.gateway.model.common;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehiclePositionHistory {
    private String system;

    @Min(1)
    @Max(64)
    private String length;
    
    private List<HistoryEntry> historyEntry;

    public VehiclePositionHistory() {
        this.historyEntry = new ArrayList<>(); 
    }
}