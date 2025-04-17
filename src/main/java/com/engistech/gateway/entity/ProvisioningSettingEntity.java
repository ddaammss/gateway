package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "provisioning_setting")
public class ProvisioningSettingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "provisioning_setting_id_gen")
    @SequenceGenerator(name = "provisioning_setting_id_gen", sequenceName = "provisioning_setting_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "header_id", nullable = false)
    private Integer headerId;

    @Size(max = 25)
    @NotNull
    @Column(name = "brand", nullable = false, length = 25)
    private String brand;

    @Column(name = "dhc_interval_value")
    private Integer dhcIntervalValue;

    @NotNull
    @Column(name = "callback_standby_timer", nullable = false)
    private Integer callbackStandbyTimer;

    @NotNull
    @Column(name = "sos_cancel_timer", nullable = false)
    private Integer sosCancelTimer;

    @Size(max = 5)
    @NotNull
    @Column(name = "provisioning_language", nullable = false, length = 5)
    private String provisioningLanguage;

}