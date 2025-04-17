package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "vehicle_position_history")
public class VehiclePositionHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicle_position_history_id_gen")
    @SequenceGenerator(name = "vehicle_position_history_id_gen", sequenceName = "vehicle_position_history_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "vehicle_report_id", nullable = false)
    private Integer vehicleReportId;

    @NotNull
    @Column(name = "entry_no", nullable = false)
    private Integer entryNo;

    @Size(max = 20)
    @NotNull
    @Column(name = "coordinate_lat", nullable = false, length = 20)
    private String coordinateLat;

    @Size(max = 20)
    @NotNull
    @Column(name = "coordinate_lon", nullable = false, length = 20)
    private String coordinateLon;

    @NotNull
    @Column(name = "date_time_utc", nullable = false)
    private String dateTimeUtc;

    @Size(max = 5)
    @NotNull
    @Column(name = "coordinate_unit", nullable = false, length = 5)
    private String coordinateUnit;

}