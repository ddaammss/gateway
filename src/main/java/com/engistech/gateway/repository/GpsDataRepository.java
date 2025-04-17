package com.engistech.gateway.repository;

import com.engistech.gateway.entity.GpsDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpsDataRepository extends JpaRepository<GpsDataEntity, Integer> {
}