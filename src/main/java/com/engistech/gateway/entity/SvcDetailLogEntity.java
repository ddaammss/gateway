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
@Table(name = "tbl_tele_svc_detail_log", schema = "telemetry")
public class SvcDetailLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tele_svc_detail_id_gen")
    @SequenceGenerator(name = "tele_svc_detail_id_gen", sequenceName = "telemetry.tbl_tele_svc_detail_log_tele_svc_detail_id_seq", allocationSize = 1)
    @Column(nullable = false)
    private Long teleSvcDetailId;

    @Column(nullable = false)
    private Long teleSvcMainId;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String dest;

    @Column(nullable = true)
    private String payload;

    @Column(nullable = false)
    private String resultStatus;

    @Column(nullable = true)
    private String resultCode;

    @Column(nullable = true)
    private String resultMessage;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdTime;

    @Column(nullable = true)
    private String content;

    @Column(nullable = true)
    private Integer messageType;

    @Column(nullable = true)
    private Integer messageService;

    @Column(nullable = true)
    private Integer messageOperation;

    @Column(nullable = true)
    private Integer nackCode;
}
