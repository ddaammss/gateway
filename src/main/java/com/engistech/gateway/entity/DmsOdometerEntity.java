package com.engistech.gateway.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "if_dms_vehicle_odometer", schema = "legacy")
public class DmsOdometerEntity {

    @Id
    @Column(nullable = false)
    private String vin;

    @Column(nullable = false)
    private String ifDatetime;

    @NotNull
    @Column(nullable = false)
    private long odometers;

    @NotNull
    @Column(nullable = false)
    private String dmsProcessed;
}
