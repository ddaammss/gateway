package com.engistech.gateway.entity;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_user_logs", schema = "log")
public class UserLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_log_id_gen")
    @SequenceGenerator(name = "user_log_id_gen", sequenceName = "log.seq_user_logs_user_log_id", allocationSize = 1)
    private Long userLogId;

    @Column(nullable = true)
    private Integer vehicleId;

    @Column(nullable = true)
    private String providedBy;

    @Column(nullable = true)
    private Short pktType;

    @Column(nullable = true)
    private Integer userId;

    @Column(nullable = true)
    private Short receivedType;

    @Column(nullable = true)
    private OffsetDateTime gpsAccessStartTime;

    @Column(nullable = true)
    private OffsetDateTime gpsAccessEndTime;

    @PrePersist
    public void setDefaultValues() {
        if (this.providedBy == null) {
            this.providedBy = "토요타 코리아";
        }

        if (this.receivedType == null) {
            this.receivedType = (short) 2;
        }

        if (this.gpsAccessStartTime == null) {
            this.gpsAccessStartTime = OffsetDateTime.now();
        }

        if (this.gpsAccessEndTime == null) {
            this.gpsAccessEndTime = OffsetDateTime.now();
        }
    }
}