package com.engistech.gateway.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.engistech.gateway.entity.DmsOdometerEntity;

public interface DmsOdometerEntityRepository extends JpaRepository<DmsOdometerEntity, String> {

    @Transactional
    @Modifying
    @Query("DELETE FROM DmsOdometerEntity d WHERE d.dmsProcessed = :status")
    void deleteByDmsProcessed(@Param("status") String status);
}