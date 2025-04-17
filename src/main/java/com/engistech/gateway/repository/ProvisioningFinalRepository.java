package com.engistech.gateway.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.engistech.gateway.entity.ProvisioningFinalEntity;

public interface ProvisioningFinalRepository extends JpaRepository<ProvisioningFinalEntity, Integer> {
    Optional<ProvisioningFinalEntity> findByVin(String vin);
    Optional<ProvisioningFinalEntity> findByDefaultFlag(int flag);
}