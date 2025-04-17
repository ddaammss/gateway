package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * packageName    : com.engistech.gateway.entity
 * fileName       : VehicleReport
 * author         : jjj
 * date           : 2024-12-27
 * description    : 차량 Report 정보 Entity
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-12-27        jjj       최초 생성
 */
@Entity
@Getter
@Setter
@Table(name = "vehicle_report")
public class VehicleReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicle_report_id_gen")
    @SequenceGenerator(name = "vehicle_report_id_gen", sequenceName = "vehicle_report_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "bub_in_use", nullable = false)
    private Integer bubInUse;

    @Column(name = "delta_v_range_limit")
    private Integer deltaVRangeLimit;

    @Column(name = "event_timestamp_utc", nullable = false)
    private String eventTimestampUtc;

    @Column(name = "event_trigger", nullable = false)
    private Integer eventTrigger;

    @Column(name = "front_airbag")
    private Integer frontAirbag;

    @Size(max = 8)
    @Column(name = "fuel_type", nullable = false, length = 8)
    private String fuelType;

    @NotNull
    @Column(name = "header_id", nullable = false)
    private Integer headerId;

    @NotNull
    @Column(name = "ignition_state", nullable = false)
    private Integer ignitionState;

    @Column(name = "multiple_impact")
    private Integer multipleImpact;

    @Column(name = "number_of_occupants")
    private Integer numberOfOccupants;

    @Column(name = "rear_impact")
    private Integer rearImpact;

    @Column(name = "rollover_state")
    private Integer rolloverState;

    @Column(name = "side_airbag")
    private Integer sideAirbag;

    @Column(name = "side_impact_sensor")
    private Integer sideImpactSensor;

    @Size(max = 8)
    @Column(name = "fuel_level", length = 8)
    private String fuelLevel;

    @Column(name = "under_repair_state")
    private Integer underRepairState;

}
