package com.engistech.gateway.repository;

import com.engistech.gateway.entity.VehicleReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleReportRepository extends JpaRepository<VehicleReportEntity, Integer> {
}