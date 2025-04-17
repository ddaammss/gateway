package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *packageName    : com.engistech.gateway.entity
 * fileName       : ErrorInfo
 * author         : jjj
 * date           : 2024-12-27
 * description    : 에러정보 Entity
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-12-27        jjj       최초 생성
 */
@Entity
@Getter
@Setter
@Table(name = "error_info")
public class ErrorInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "error_info_gen")
    @SequenceGenerator(name = "error_info_gen", sequenceName = "error_info_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "error_code")
    private Integer errorCode;

    @Size(max = 255)
    @Column(name = "error_desc")
    private String errorDesc;

    @NotNull
    @Column(name = "header_id", nullable = false)
    private Integer headerId;

}
