package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@Table(name = "vls_report_setting")
public class VlsReportSettingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vls_report_setting_id_gen")
    @SequenceGenerator(name = "vls_report_setting_id_gen", sequenceName = "vls_report_setting_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "header_id", nullable = false)
    private Integer headerId;

    @NotNull
    @Column(name = "priority_div", nullable = false)
    private Integer priorityDiv;

    @NotNull
    @Column(name = "activate_time_limit", nullable = false)
    private Integer activateTimeLimit;

    @NotNull
    @Column(name = "time_limit_value", nullable = false)
    private Integer timeLimitValue;

    @NotNull
    @Column(name = "ignition_on_report", nullable = false)
    private Integer ignitionOnReport;

    @NotNull
    @Column(name = "ignition_off_report", nullable = false)
    private Integer ignitionOffReport;

    @NotNull
    @Column(name = "activate_time_interval", nullable = false)
    private Integer activateTimeInterval;

    @NotNull
    @Column(name = "interval_value", nullable = false)
    private Integer intervalValue;

    @NotNull
    @Column(name = "history_report", nullable = false)
    private Integer historyReport;

    @NotNull
    @Column(name = "time_limit_unit", nullable = false)
    private Integer timeLimitUnit;

    @NotNull
    @Column(name = "interval_unit", nullable = false)
    private Integer intervalUnit;

}