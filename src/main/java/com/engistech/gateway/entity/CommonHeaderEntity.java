package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * packageName    : com.engistech.gateway.entity
 * fileName       : CommonHeader
 * author         : jjj
 * date           : 2024-12-27
 * description    : Header 정보 (DCM 및 Message 구분 정보) Entity
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-12-27        jjj       최초 생성
 */
@Entity
@Getter
@Setter
@Table(name = "common_header")
public class CommonHeaderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "common_header_id_gen")
    @SequenceGenerator(name = "common_header_id_gen", sequenceName = "common_header_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 36)
    @NotNull
    @Column(name = "correlation_id", nullable = false, length = 36)
    private String correlationId;

    @NotNull
    @Column(name = "transmission_timestamp_utc", nullable = false)
    private String transmissionTimestampUtc;

    @Size(max = 20)
    @Column(name = "dcm_fw_version", nullable = false, length = 20)
    private String dcmFwVersion;

    @Size(max = 50)
    @Column(name = "dcm_supplier", nullable = false, length = 50)
    private String dcmSupplier;

    @Size(max = 32)
    @Column(name = "euiccid", nullable = false, length = 32)
    private String euiccid;

    @Size(max = 10)
    @Column(name = "hu_language", length = 10)
    private String huLanguage;

    @Size(max = 30)
    @Column(name = "iccid", nullable = false, length = 30)
    private String iccid;

    @Size(max = 20)
    @Column(name = "imei", nullable = false, length = 20)
    private String imei;

    @Size(max = 20)
    @Column(name = "imsi", nullable = false, length = 20)
    private String imsi;

    @Size(max = 20)
    @Column(name = "mcu_sw_version", nullable = false, length = 20)
    private String mcuSwVersion;

    @Size(max = 20)
    @Column(name = "msisdn", nullable = false, length = 20)
    private String msisdn;

    @Size(max = 20)
    @Column(name = "nad_fw_version", nullable = false, length = 20)
    private String nadFwVersion;

    @Size(max = 10)
    @Column(name = "schema_version", nullable = false, length = 10)
    private String schemaVersion;

    @Size(max = 50)
    @NotNull
    @Column(name = "session_id", nullable = false, length = 50)
    private String sessionId;

    @Size(max = 50)
    @Column(name = "user_agent", nullable = false, length = 50)
    private String userAgent;

    @Size(max = 17)
    @Column(name = "vin", nullable = false, length = 17)
    private String vin;

    @NotNull
    @Column(name = "message_operation", nullable = false)
    private Integer messageOperation;

    @NotNull
    @Column(name = "message_service", nullable = false)
    private Integer messageService;

    @NotNull
    @Column(name = "message_type", nullable = false)
    private Integer messageType;
}
