package com.engistech.gateway.repository;

import com.engistech.gateway.entity.VehiclePositionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiclePositionHistoryRepository extends JpaRepository<VehiclePositionHistoryEntity, Integer> {
}