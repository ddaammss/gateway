package com.engistech.gateway.repository;

import com.engistech.gateway.entity.CellularInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CellularInfoRepository extends JpaRepository<CellularInfoEntity, Integer> {
}