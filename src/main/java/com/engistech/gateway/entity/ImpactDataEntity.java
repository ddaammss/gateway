package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "impact_data")
public class ImpactDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "impact_data_id_gen")
    @SequenceGenerator(name = "impact_data_id_gen", sequenceName = "impact_data_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "vehicle_report_id", nullable = false)
    private Integer vehicleReportId;

    @NotNull
    @Column(name = "impact_type", nullable = false, precision = 1)
    private int impactType;

    @NotNull
    @Column(name = "max_delta_vy", nullable = false)
    private String maxDeltaVy;

    @NotNull
    @Column(name = "max_delta_vx", nullable = false)
    private String maxDeltaVx;

}