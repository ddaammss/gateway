package com.engistech.gateway.model.common;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Provisioning {
    private List<ServiceFlag> serviceFlags;
    private String brand;
    private String provisioningLanguage;
    private Configuration configuration;

    public Provisioning() {
        this.serviceFlags = new ArrayList<>(); 
        this.configuration = new Configuration();
    }
}