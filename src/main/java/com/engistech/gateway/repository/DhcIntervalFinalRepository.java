package com.engistech.gateway.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.engistech.gateway.entity.DhcIntervalFinalEntity;

public interface DhcIntervalFinalRepository extends JpaRepository<DhcIntervalFinalEntity, Integer> {
    Optional<DhcIntervalFinalEntity> findByVin(String vin);
}