package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@Table(name = "seat_status")
public class SeatStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seat_status_id_gen")
    @SequenceGenerator(name = "seat_status_id_gen", sequenceName = "seat_status_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "vehicle_report_id", nullable = false)
    private Integer vehicleReportId;

    @NotNull
    @Column(name = "seat_position_type", nullable = false)
    private Integer seatPositionType;

    @Column(name = "occupant_state", nullable = false)
    private Integer occupantState;

    @NotNull
    @Column(name = "buckle_state", nullable = false)
    private Integer buckleState;

}