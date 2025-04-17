package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "provisioning_preset")
public class ProvisioningPresetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "provisioning_preset_id_gen")
    @SequenceGenerator(name = "provisioning_preset_id_gen", sequenceName = "provisioning_preset_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "default_flag", nullable = false)
    private Integer defaultFlag;

    @Size(max = 50)
    @NotNull
    @Column(name = "preset_name", nullable = false, length = 50)
    private String presetName;

    @NotNull
    @Column(name = "acn_service_flag", nullable = false)
    private Integer acnServiceFlag;

    @NotNull
    @Column(name = "sos_service_flag", nullable = false)
    private Integer sosServiceFlag;

    @NotNull
    @Column(name = "vls_service_flag", nullable = false)
    private Integer vlsServiceFlag;

    @NotNull
    @Column(name = "rsn_service_flag", nullable = false)
    private Integer rsnServiceFlag;

    @NotNull
    @Column(name = "dhc_service_flag", nullable = false)
    private Integer dhcServiceFlag;

    @NotNull
    @Column(name = "dhc_interval_value", nullable = false)
    private Integer dhcIntervalValue;

    @Size(max = 25)
    @NotNull
    @Column(name = "brand", nullable = false, length = 25)
    private String brand;

    @Size(max = 5)
    @NotNull
    @Column(name = "provisioning_language", nullable = false, length = 5)
    private String provisioningLanguage;

    @Size(max = 25)
    @NotNull
    @Column(name = "acn_phone_primary", nullable = false, length = 25)
    private String acnPhonePrimary;

    @Size(max = 25)
    @Column(name = "acn_phone_secondary", length = 25)
    private String acnPhoneSecondary;

    @Size(max = 25)
    @Column(name = "acn_phone_third", length = 25)
    private String acnPhoneThird;

    @Size(max = 25)
    @Column(name = "acn_phone_fourth", length = 25)
    private String acnPhoneFourth;

    @Size(max = 25)
    @Column(name = "acn_phone_fifth", length = 25)
    private String acnPhoneFifth;

    @Size(max = 25)
    @Column(name = "acn_phone_sixth", length = 25)
    private String acnPhoneSixth;

    @Size(max = 25)
    @Column(name = "acn_phone_seventh", length = 25)
    private String acnPhoneSeventh;

    @Size(max = 25)
    @Column(name = "acn_phone_eighth", length = 25)
    private String acnPhoneEighth;

    @Size(max = 25)
    @Column(name = "acn_phone_ninth", length = 25)
    private String acnPhoneNinth;

    @Size(max = 25)
    @Column(name = "acn_phone_tenth", length = 25)
    private String acnPhoneTenth;

    @Size(max = 25)
    @NotNull
    @Column(name = "sos_phone_primary", nullable = false, length = 25)
    private String sosPhonePrimary;

    @Size(max = 25)
    @Column(name = "sos_phone_secondary", length = 25)
    private String sosPhoneSecondary;

    @Size(max = 25)
    @Column(name = "sos_phone_third", length = 25)
    private String sosPhoneThird;

    @Size(max = 25)
    @Column(name = "sos_phone_fourth", length = 25)
    private String sosPhoneFourth;

    @Size(max = 25)
    @Column(name = "sos_phone_fifth", length = 25)
    private String sosPhoneFifth;

    @Size(max = 25)
    @Column(name = "sos_phone_sixth", length = 25)
    private String sosPhoneSixth;

    @Size(max = 25)
    @Column(name = "sos_phone_seventh", length = 25)
    private String sosPhoneSeventh;

    @Size(max = 25)
    @Column(name = "sos_phone_eighth", length = 25)
    private String sosPhoneEighth;

    @Size(max = 25)
    @Column(name = "sos_phone_ninth", length = 25)
    private String sosPhoneNinth;

    @Size(max = 25)
    @Column(name = "sos_phone_tenth", length = 25)
    private String sosPhoneTenth;

    @Size(max = 25)
    @NotNull
    @Column(name = "rsn_phone_primary", nullable = false, length = 25)
    private String rsnPhonePrimary;

    @Size(max = 25)
    @Column(name = "rsn_phone_secondary", length = 25)
    private String rsnPhoneSecondary;

    @Size(max = 25)
    @Column(name = "rsn_phone_third", length = 25)
    private String rsnPhoneThird;

    @Size(max = 25)
    @Column(name = "rsn_phone_fourth", length = 25)
    private String rsnPhoneFourth;

    @Size(max = 25)
    @Column(name = "rsn_phone_fifth", length = 25)
    private String rsnPhoneFifth;

    @Size(max = 25)
    @Column(name = "rsn_phone_sixth", length = 25)
    private String rsnPhoneSixth;

    @Size(max = 25)
    @Column(name = "rsn_phone_seventh", length = 25)
    private String rsnPhoneSeventh;

    @Size(max = 25)
    @Column(name = "rsn_phone_eighth", length = 25)
    private String rsnPhoneEighth;

    @Size(max = 25)
    @Column(name = "rsn_phone_ninth", length = 25)
    private String rsnPhoneNinth;

    @Size(max = 25)
    @Column(name = "rsn_phone_tenth", length = 25)
    private String rsnPhoneTenth;

    @Size(max = 25)
    @NotNull
    @Column(name = "vls_phone_primary", nullable = false, length = 25)
    private String vlsPhonePrimary;

    @Size(max = 25)
    @Column(name = "vls_phone_secondary", length = 25)
    private String vlsPhoneSecondary;

    @Size(max = 25)
    @Column(name = "vls_phone_third", length = 25)
    private String vlsPhoneThird;

    @Size(max = 25)
    @Column(name = "vls_phone_fourth", length = 25)
    private String vlsPhoneFourth;

    @Size(max = 25)
    @Column(name = "vls_phone_fifth", length = 25)
    private String vlsPhoneFifth;

    @Size(max = 25)
    @Column(name = "vls_phone_sixth", length = 25)
    private String vlsPhoneSixth;

    @Size(max = 25)
    @Column(name = "vls_phone_seventh", length = 25)
    private String vlsPhoneSeventh;

    @Size(max = 25)
    @Column(name = "vls_phone_eighth", length = 25)
    private String vlsPhoneEighth;

    @Size(max = 25)
    @Column(name = "vls_phone_ninth", length = 25)
    private String vlsPhoneNinth;

    @Size(max = 25)
    @Column(name = "vls_phone_tenth", length = 25)
    private String vlsPhoneTenth;

    @Size(max = 25)
    @NotNull
    @Column(name = "inbound_phone_primary", nullable = false, length = 25)
    private String inboundPhonePrimary;

    @Size(max = 25)
    @Column(name = "inbound_phone_secondary", length = 25)
    private String inboundPhoneSecondary;

    @Size(max = 25)
    @Column(name = "inbound_phone_third", length = 25)
    private String inboundPhoneThird;

    @Size(max = 25)
    @Column(name = "inbound_phone_fourth", length = 25)
    private String inboundPhoneFourth;

    @Size(max = 25)
    @Column(name = "inbound_phone_fifth", length = 25)
    private String inboundPhoneFifth;

    @Size(max = 25)
    @Column(name = "inbound_phone_sixth", length = 25)
    private String inboundPhoneSixth;

    @Size(max = 25)
    @Column(name = "inbound_phone_seventh", length = 25)
    private String inboundPhoneSeventh;

    @Size(max = 25)
    @Column(name = "inbound_phone_eighth", length = 25)
    private String inboundPhoneEighth;

    @Size(max = 25)
    @Column(name = "inbound_phone_ninth", length = 25)
    private String inboundPhoneNinth;

    @Size(max = 25)
    @Column(name = "inbound_phone_tenth", length = 25)
    private String inboundPhoneTenth;

    @NotNull
    @Column(name = "callback_standby_timer", nullable = false)
    private Integer callbackStandbyTimer;

    @NotNull
    @Column(name = "sos_cancel_timer", nullable = false)
    private Integer sosCancelTimer;

    @NotNull
    @Column(name = "created_dt", nullable = false)
    private Instant createdDt;

}