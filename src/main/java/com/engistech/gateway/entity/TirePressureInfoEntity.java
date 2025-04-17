package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "tire_pressure_info")
public class TirePressureInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tire_pressure_info_id_gen")
    @SequenceGenerator(name = "tire_pressure_info_id_gen", sequenceName = "tire_pressure_info_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "dhc_report_id", nullable = false)
    private Integer dhcReportId;

    @Size(max = 7)
    @NotNull
    @Column(name = "tire_pressure_unit", nullable = false, length = 7)
    private String tirePressureUnit;

    @Size(max = 7)
    @NotNull
    @Column(name = "tire1_value", nullable = false, length = 7)
    private String tire1Value;

    @Size(max = 7)
    @NotNull
    @Column(name = "tire2_value", nullable = false, length = 7)
    private String tire2Value;

    @Size(max = 7)
    @NotNull
    @Column(name = "tire3_value", nullable = false, length = 7)
    private String tire3Value;

    @Size(max = 7)
    @NotNull
    @Column(name = "tire4_value", nullable = false, length = 7)
    private String tire4Value;

    @Size(max = 7)
    @NotNull
    @Column(name = "tire5_value", nullable = false, length = 7)
    private String tire5Value;

}