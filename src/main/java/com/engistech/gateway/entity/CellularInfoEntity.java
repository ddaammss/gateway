package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "cellular_info")
public class CellularInfoEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cellular_info_id_gen")
    @SequenceGenerator(name = "cellular_info_id_gen", sequenceName = "cellular_info_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "report_id", nullable = false)
    private Integer reportId;

    @Size(max = 10)
    @NotNull
    @Column(name = "mnc", nullable = false, length = 10)
    private String mnc;

    @Size(max = 10)
    @NotNull
    @Column(name = "rat", nullable = false, length = 10)
    private String rat;

    @Size(max = 10)
    @NotNull
    @Column(name = "tac", nullable = false, length = 10)
    private String tac;

    @Size(max = 10)
    @NotNull
    @Column(name = "cell_id", nullable = false, length = 10)
    private String cellId;

    @Size(max = 10)
    @NotNull
    @Column(name = "mcc", nullable = false, length = 10)
    private String mcc;

    @NotNull
    @Column(name = "report_div", nullable = false)
    private Integer reportDiv;

}