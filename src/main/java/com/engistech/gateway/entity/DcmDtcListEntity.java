package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "dcm_dtc_list")
public class DcmDtcListEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dcm_dtc_list_id_gen")
    @SequenceGenerator(name = "dcm_dtc_list_id_gen", sequenceName = "dcm_dtc_list_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "dhc_report_id", nullable = false)
    private Integer dhcReportId;

    @Size(max = 10)
    @NotNull
    @Column(name = "dtc_code", nullable = false, length = 10)
    private String dtcCode;

    @Size(max = 4)
    @NotNull
    @Column(name = "failure_type", nullable = false, length = 4)
    private String failureType;

    @NotNull
    @Column(name = "test_failed_status", nullable = false)
    private Integer testFailedStatus;

    @NotNull
    @Column(name = "confirmed_dtc_status", nullable = false)
    private Integer confirmedDtcStatus;

    @NotNull
    @Column(name = "test_failed_since_last_clear_status", nullable = false)
    private Integer testFailedSinceLastClearStatus;

    @NotNull
    @Column(name = "ssr_time_information_utc", nullable = false)
    private String ssrTimeInformationUtc;

    @Size(max = 5)
    @NotNull
    @Column(name = "ssr_voltage", nullable = false, length = 5)
    private String ssrVoltage;

}