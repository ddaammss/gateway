package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "dhc_interval")
public class DhcIntervalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dhc_interval_id_gen")
    @SequenceGenerator(name = "dhc_interval_id_gen", sequenceName = "dhc_interval_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "header_id", nullable = false)
    private Integer headerId;

    @NotNull
    @Column(name = "interval_value", nullable = false)
    private Integer intervalValue;

}