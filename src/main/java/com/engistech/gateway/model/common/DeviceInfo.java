package com.engistech.gateway.model.common;

import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DeviceInfo {
    
    private String dcmSupplier;
    private String dcmfwVersion;
    private String nadfwVersion;
    private String mcuswVersion;
    private String msisdn;
    private String imsi;
    private String imei;
    private String vin;
    private String iccid;
    private String euiccid;
    
    @JsonProperty("DCMSupplier")@NotNull
    public String getDcmSupplier() {
        return dcmSupplier;
    }

    public void setDcmSupplier(String dcmSupplier) {
        this.dcmSupplier = dcmSupplier;
    }
    
    @JsonProperty("DCMFWVersion")@NotNull
    public String getDcmfwVersion() {
        return dcmfwVersion;
    }

    public void setDcmfwVersion(String dcmfwVersion) {
        this.dcmfwVersion = dcmfwVersion;
    }

    @JsonProperty("NADFWVersion")@NotNull
    public String getNadfwVersion() {
        return nadfwVersion;
    }

    public void setNadfwVersion(String nadfwVersion) {
        this.nadfwVersion = nadfwVersion;
    }
    
    @JsonProperty("MCUSWVersion")@NotNull
    public String getMcuswVersion() {
        return mcuswVersion;
    }

    public void setMcuswVersion(String mcuswVersion) {
        this.mcuswVersion = mcuswVersion;
    }

    @JsonProperty("MSISDN")@NotNull
    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    @JsonProperty("IMSI")@NotNull
    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }
    
    @JsonProperty("IMEI")@NotNull
    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    @JsonProperty("VIN")@NotNull
    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }
    
    @JsonProperty("ICCID")@NotNull
    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }
    
    @JsonProperty("EUICCID")@NotNull
    public String getEuiccid() {
        return euiccid;
    }

    public void setEuiccid(String euiccid) {
        this.euiccid = euiccid;
    }
}