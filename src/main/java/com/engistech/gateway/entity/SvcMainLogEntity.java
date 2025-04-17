package com.engistech.gateway.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_tele_svc_main_log", schema = "telemetry")
public class SvcMainLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tele_svc_main_id_gen")
    @SequenceGenerator(name = "tele_svc_main_id_gen", sequenceName = "telemetry.tbl_tele_svc_main_log_tele_svc_main_id_seq", allocationSize = 1)
    @Column(nullable = false)
    private Long teleSvcMainId;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = true)
    private String vin;

    @Column(nullable = true)
    private String msin;

    @Column(nullable = true)
    private String imei;

    @Column(nullable = true)
    private String iccid;

    @Column(nullable = false)
    private String teleSvcType;

    @Column(nullable = false)
    private LocalDateTime teleSvcTime;

    @Column(nullable = false)
    private String teleSvcTimeOffset;

    @Column(nullable = true)
    private String teleSvcStatusCode;

    @Column(nullable = false)
    private LocalDateTime teleSvcStatusTime;

    @Column(nullable = true)
    private String requestSource;
}
