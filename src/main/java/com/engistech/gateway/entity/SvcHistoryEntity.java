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
@Table(name = "tbl_telemetry_service_history", schema = "telemetry")
public class SvcHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "telemetry_service_history_id_gen")
    @SequenceGenerator(name = "telemetry_service_history_id_gen", sequenceName = "telemetry.tbl_telemetry_service_history_telemetry_service_history_id_seq", allocationSize = 1)
    private Long telemetryServiceHistoryId;

    @Column(nullable = false)
    private Short telemetryServiceType;

    @Column(nullable = false)
    private Integer vehicleId;

    @Column(insertable = false)
    private OffsetDateTime serviceTime;

    @Column(nullable = true)
    private Integer serviceTimeOffset;

    @Column(nullable = true)
    private Integer serviceStatus;
    
    @PrePersist
    public void setDefaultValues() {
        if (this.serviceTimeOffset == null) {
            this.serviceTimeOffset = 9;
        }
    }
}
