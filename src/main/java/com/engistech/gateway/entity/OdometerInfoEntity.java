package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "odometer_info")
public class OdometerInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "odometer_info_id_gen")
    @SequenceGenerator(name = "odometer_info_id_gen", sequenceName = "odometer_info_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "dhc_report_id", nullable = false)
    private Integer dhcReportId;

    @Size(max = 7)
    @NotNull
    @Column(name = "odometer_unit", nullable = false, length = 7)
    private String odometerUnit;

    @Size(max = 32)
    @NotNull
    @Column(name = "odometer_value", nullable = false, length = 32)
    private String odometerValue;

}