package com.engistech.gateway.model.common;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CellularInfo {
    
    private String mmc;
    private String rat;

    @Min(1)
    @Max(65535)
    private String tac;

    @Min(1)
    @Max(68719476735L)
    private String cellId;
    
    private String mcc;

    @JsonProperty("MNC")
    public String getMmc() {
        return mmc;
    }

    public void setMmc(String mmc) {
        this.mmc = mmc;
    }

    @JsonProperty("RAT")
    public String getRat() {
        return rat;
    }

    public void setRat(String rat) {
        this.rat = rat;
    }

    @JsonProperty("TAC")
    public String getTac() {
        return tac;
    }

    public void setTac(String tac) {
        this.tac = tac;
    }

    @JsonProperty("CellID")
    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    @JsonProperty("MCC")
    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }
}