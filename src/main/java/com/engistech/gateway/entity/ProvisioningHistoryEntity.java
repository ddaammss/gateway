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
@Table(name = "provisioning_history")
public class ProvisioningHistoryEntity {

    // 각 컬럼의 디폴트값 적용을 위해 nullable 설정을 주석 처리함
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "provisioning_history_id_gen")
    @SequenceGenerator(name = "provisioning_history_id_gen", sequenceName = "provisioning_history_id_seq", allocationSize = 1)
    //@Column(nullable = false)
    private Integer id;

    //@Column(nullable = false)
    private String vin;

    //@Column(nullable = false)
    private Integer acnServiceFlag;

    //@Column(nullable = false)
    private Integer sosServiceFlag;

    //@Column(nullable = false)
    private Integer vlsServiceFlag;

    //@Column(nullable = false)
    private Integer rsnServiceFlag;

    //@Column(nullable = false)
    private Integer dhcServiceFlag;

    //@Column(nullable = false)
    private String brand;

    //@Column(nullable = false)
    private String provisioningLanguage;

    //@Column(nullable = false)
    private String acnPhonePrimary;

    //@Column(nullable = true)
    private String acnPhoneSecondary;

    //@Column(nullable = false)
    private String sosPhonePrimary;

    //@Column(nullable = true)
    private String sosPhoneSecondary;

    //@Column(nullable = false)
    private String rsnPhonePrimary;

    //@Column(nullable = true)
    private String rsnPhoneSecondary;

    //@Column(nullable = false)
    private String vlsPhonePrimary;

    //@Column(nullable = true)
    private String vlsPhoneSecondary;

    //@Column(nullable = false)
    private String inboundPhonePrimary;

    //@Column(nullable = true)
    private String inboundPhoneSecondary;

    //@Column(nullable = false)
    private Integer callbackStandbyTimer;

    //@Column(nullable = false)
    private Integer sosCancelTimer;

    //@Column(nullable = true)
    private Integer userId;

    //@Column(nullable = false)
    private java.sql.Timestamp createdDt;
}