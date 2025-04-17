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
@Table(name = "provisioning_final")
public class ProvisioningFinalEntity {

    // 각 컬럼의 디폴트값 적용을 위해 nullable 설정을 주석 처리함
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "provisioning_final_id_gen")
    @SequenceGenerator(name = "provisioning_final_id_gen", sequenceName = "provisioning_final_id_seq", allocationSize = 1)
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

    //@Column(nullable = true)
    private String acnPhoneThird;

    //@Column(nullable = true)
    private String acnPhoneFourth;

    //@Column(nullable = true)
    private String acnPhoneFifth;

    //@Column(nullable = true)
    private String acnPhoneSixth;

    //@Column(nullable = true)
    private String acnPhoneSeventh;

    //@Column(nullable = true)
    private String acnPhoneEighth;

    //@Column(nullable = true)
    private String acnPhoneNinth;

    //@Column(nullable = true)
    private String acnPhoneTenth;

    //@Column(nullable = false)
    private String sosPhonePrimary;

    //@Column(nullable = true)
    private String sosPhoneSecondary;

    //@Column(nullable = true)
    private String sosPhoneThird;

    //@Column(nullable = true)
    private String sosPhoneFourth;

    //@Column(nullable = true)
    private String sosPhoneFifth;

    //@Column(nullable = true)
    private String sosPhoneSixth;

    //@Column(nullable = true)
    private String sosPhoneSeventh;

    //@Column(nullable = true)
    private String sosPhoneEighth;

    //@Column(nullable = true)
    private String sosPhoneNinth;

    //@Column(nullable = true)
    private String sosPhoneTenth;

    //@Column(nullable = false)
    private String rsnPhonePrimary;

    //@Column(nullable = true)
    private String rsnPhoneSecondary;

    //@Column(nullable = true)
    private String rsnPhoneThird;

    //@Column(nullable = true)
    private String rsnPhoneFourth;

    //@Column(nullable = true)
    private String rsnPhoneFifth;

    //@Column(nullable = true)
    private String rsnPhoneSixth;

    //@Column(nullable = true)
    private String rsnPhoneSeventh;

    //@Column(nullable = true)
    private String rsnPhoneEighth;

    //@Column(nullable = true)
    private String rsnPhoneNinth;

    //@Column(nullable = true)
    private String rsnPhoneTenth;

    //@Column(nullable = false)
    private String vlsPhonePrimary;

    //@Column(nullable = true)
    private String vlsPhoneSecondary;

    //@Column(nullable = true)
    private String vlsPhoneThird;

    //@Column(nullable = true)
    private String vlsPhoneFourth;

    //@Column(nullable = true)
    private String vlsPhoneFifth;

    //@Column(nullable = true)
    private String vlsPhoneSixth;

    //@Column(nullable = true)
    private String vlsPhoneSeventh;

    //@Column(nullable = true)
    private String vlsPhoneEighth;

    //@Column(nullable = true)
    private String vlsPhoneNinth;

    //@Column(nullable = true)
    private String vlsPhoneTenth;

    //@Column(nullable = false)
    private String inboundPhonePrimary;

    //@Column(nullable = true)
    private String inboundPhoneSecondary;

    //@Column(nullable = true)
    private String inboundPhoneThird;

    //@Column(nullable = true)
    private String inboundPhoneFourth;

    //@Column(nullable = true)
    private String inboundPhoneFifth;

    //@Column(nullable = true)
    private String inboundPhoneSixth;

    //@Column(nullable = true)
    private String inboundPhoneSeventh;

    //@Column(nullable = true)
    private String inboundPhoneEighth;

    //@Column(nullable = true)
    private String inboundPhoneNinth;

    //@Column(nullable = true)
    private String inboundPhoneTenth;

    //@Column(nullable = false)
    private Integer callbackStandbyTimer;

    //@Column(nullable = false)
    private Integer sosCancelTimer;

    //@Column(nullable = true)
    private Integer userId;

    //@Column(nullable = false)
    private java.sql.Timestamp createdDt;

    private Integer defaultFlag;
}
