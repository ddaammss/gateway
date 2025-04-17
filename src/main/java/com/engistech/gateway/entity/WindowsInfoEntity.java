package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "windows_info")
public class WindowsInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "windows_info_id_gen")
    @SequenceGenerator(name = "windows_info_id_gen", sequenceName = "windows_info_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "dhc_report_id", nullable = false)
    private Integer dhcReportId;

    @Size(max = 7)
    @NotNull
    @Column(name = "driver_seat", nullable = false, length = 7)
    private String driverSeat;

    @Size(max = 7)
    @NotNull
    @Column(name = "passenger_seat", nullable = false, length = 7)
    private String passengerSeat;

    @Size(max = 7)
    @NotNull
    @Column(name = "left_rear_seat", nullable = false, length = 7)
    private String leftRearSeat;

    @Size(max = 7)
    @NotNull
    @Column(name = "right_rear_sear", nullable = false, length = 7)
    private String rightRearSear;

    @Size(max = 7)
    @NotNull
    @Column(name = "slide_roof", nullable = false, length = 7)
    private String slideRoof;

}