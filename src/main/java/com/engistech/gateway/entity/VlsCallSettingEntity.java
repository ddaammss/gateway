package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@Table(name = "vls_call_setting")
public class VlsCallSettingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vls_call_setting_id_gen")
    @SequenceGenerator(name = "vls_call_setting_id_gen", sequenceName = "vls_call_setting_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "header_id", nullable = false)
    private Integer headerId;

    @NotNull
    @Column(name = "hmi", nullable = false)
    private Integer hmi;

}