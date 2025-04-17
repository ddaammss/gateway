package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "gps_data")
public class GpsDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gps_data_id_gen")
    @SequenceGenerator(name = "gps_data_id_gen", sequenceName = "gps_data_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "report_id", nullable = false)
    private Integer reportId;

    @NotNull
    @Column(name = "heading_direction", nullable = false)
    private Integer headingDirection;

    @Size(max = 10)
    @NotNull
    @Column(name = "gps_system", nullable = false, length = 10)
    private String gpsSystem;

    @Size(max = 20)
    @NotNull
    @Column(name = "coordinate_lat", nullable = false, length = 20)
    private String coordinateLat;

    @Size(max = 20)
    @NotNull
    @Column(name = "coordinate_lon", nullable = false, length = 20)
    private String coordinateLon;

    @Size(max = 10)
    @NotNull
    @Column(name = "velocity_unit", nullable = false, length = 10)
    private String velocityUnit;

    @NotNull
    @Column(name = "velocity_value", nullable = false)
    private Integer velocityValue;

    @NotNull
    @Column(name = "gnss_accuracy", nullable = false)
    private Integer gnssAccuracy;

    @NotNull
    @Column(name = "date_time_utc", nullable = false)
    private String dateTimeUtc;

    @NotNull
    @Column(name = "report_div", nullable = false)
    private Integer reportDiv;

    @NotNull
    @Column(name = "is_last_valid_data", nullable = false)
    private Integer isLastValidData;

    @Size(max = 5)
    @NotNull
    @Column(name = "coordinate_unit", nullable = false, length = 5)
    private String coordinateUnit;

}