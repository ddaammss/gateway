package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@Table(name = "provisioning_service_list")
public class ProvisioningServiceListEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "provisioning_service_list_id_gen")
    @SequenceGenerator(name = "provisioning_service_list_id_gen", sequenceName = "provisioning_service_list_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "provisioning_id", nullable = false)
    private Integer provisioningId;

    @NotNull
    @Column(name = "service_flag", nullable = false)
    private Integer serviceFlag;

    @NotNull
    @Column(name = "service_type", nullable = false)
    private Integer serviceType;

}