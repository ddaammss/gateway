package com.engistech.gateway.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DynamicInsert
@Table(name = "dhc_interval_final")
public class DhcIntervalFinalEntity {

    // 각 컬럼의 디폴트값 적용을 위해 nullable 설정을 주석 처리함
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dhc_interval_final_id_gen")
    @SequenceGenerator(name = "dhc_interval_final_id_gen", sequenceName = "dhc_interval_final_id_seq", allocationSize = 1)
    //@Column(nullable = false)
    private Integer id;

    //@Column(nullable = false)
    private String vin;

    //@Column(nullable = false)
    private Integer intervalValue;

    //@Column(nullable = true)
    private Integer userId;

    //@Column(nullable = false)
    private java.sql.Timestamp createdDt;
}
