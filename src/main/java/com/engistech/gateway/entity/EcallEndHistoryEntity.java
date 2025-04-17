package com.engistech.gateway.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_cti_ecall_end_history", schema = "callcenter")
public class EcallEndHistoryEntity {

    @Id
    @Column(nullable = false)
    private String historyId;

    @Column(nullable = false)
    private String callType;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = true)
    private LocalDateTime callStartTime;

    @Column(nullable = false)
    private LocalDateTime callEndTime;

    @Column(nullable = false)
    private Boolean callEndType;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private LocalDateTime createdTime;
}
