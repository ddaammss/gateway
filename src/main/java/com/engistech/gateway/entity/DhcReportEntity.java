package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "dhc_report")
public class DhcReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dhc_report_id_gen")
    @SequenceGenerator(name = "dhc_report_id_gen", sequenceName = "dhc_report_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "header_id", nullable = false)
    private Integer headerId;

    @NotNull
    @Column(name = "ignition_state", nullable = false)
    private Integer ignitionState;

    @Size(max = 8)
    @NotNull
    @Column(name = "fuel_level", nullable = false, length = 8)
    private String fuelLevel;

    @NotNull
    @Column(name = "battery_voltate", nullable = false)
    private Integer batteryVoltate;

    @NotNull
    @Column(name = "event_timestamp_utc", nullable = false)
    private String eventTimestampUtc;

}