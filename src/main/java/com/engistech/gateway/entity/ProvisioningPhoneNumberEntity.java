package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "provisioning_phone_numbers")
public class ProvisioningPhoneNumberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "provisioning_phone_numbers_id_gen")
    @SequenceGenerator(name = "provisioning_phone_numbers_id_gen", sequenceName = "provisioning_phone_numbers_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "provisioning_id", nullable = false)
    private Integer provisioningId;

    @Size(max = 25)
    @NotNull
    @Column(name = "phone_number", nullable = false, length = 25)
    private String phoneNumber;

    @NotNull
    @Column(name = "service_type", nullable = false)
    private Integer serviceType;

    @NotNull
    @Column(name = "phone_number_type", nullable = false)
    private Integer phoneNumberType;

}