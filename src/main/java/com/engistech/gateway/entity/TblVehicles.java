package com.engistech.gateway.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@ToString
@Table(name = "tbl_vehicles", schema = "vehicle")
public class TblVehicles {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "vehicle_id", nullable = false)
    private Integer id;

    @Size(max = 17)
    @NotNull
    @Column(name = "vin", nullable = false, length = 17)
    private String vin;

    @Column(name = "platform_code")
    private String platformCode;

    @Column(name = "registration_no")
    private String registrationNo;

    @Column(name = "imei")
    private String imei;

    @Column(name = "dcm_device_id")
    private Integer dcmDeviceId;

    @Column(name = "owner_user_seq")
    private Integer ownerUserSeq;

    @Column(name = "registration_date")
    private OffsetDateTime registrationDate;

    @Column(name = "vehicle_status")
    private Short vehicleStatus;

    @Column(name = "production_date")
    private OffsetDateTime productionDate;

    @Column(name = "customer_delivery_date")
    private OffsetDateTime customerDeliveryDate;

    @Size(max = 4)
    @Column(name = "dealer_code", length = 4)
    private String dealerCode;

    @Column(name = "dealer_agency_code")
    private String dealerAgencyCode;

    @Column(name = "model_year")
    private Short modelYear;

    @Column(name = "sales_corp_code")
    private String salesCorpCode;

    @Size(max = 4)
    @Column(name = "exterior_color_code", length = 4)
    private String exteriorColorCode;

    @Column(name = "engine_displacement")
    private String engineDisplacement;

    @Column(name = "engine_fuel_code")
    private String engineFuelCode;

    @Size(max = 10)
    @Column(name = "dcm_part_number", length = 10)
    private String dcmPartNumber;

    @Size(max = 50)
    @Column(name = "iccid", length = 50)
    private String iccid;

    @Size(max = 30)
    @Column(name = "contract_no", length = 30)
    private String contractNo;

    @Size(max = 20)
    @Column(name = "mdl_cd", length = 20)
    private String mdlCd;

    @Column(name = "contract_date")
    private OffsetDateTime contractDate;

    @Size(max = 20)
    @Column(name = "brand_nm", length = 20)
    private String brandNm;

    @Column(name = "last_modified_time")
    private OffsetDateTime lastModifiedTime;

}