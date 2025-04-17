package com.engistech.gateway.repository;

import com.engistech.gateway.entity.TblVehicles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TblVehiclesRepository extends JpaRepository<TblVehicles, Integer> {

    List<TblVehicles> findByDealerAgencyCode(String dealerAgencyCode);

    @Query("SELECT v.id FROM TblVehicles v WHERE v.vin = :vin")
    Optional<Integer> findVehicleIdByVin(@Param("vin") String vin);
}