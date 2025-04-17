package com.engistech.gateway.repository;

import com.engistech.gateway.entity.ProvisioningPresetEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvisioningPresetEntityRepository extends JpaRepository<ProvisioningPresetEntity, Long> {

  ProvisioningPresetEntity findByDefaultFlag(@Param("defaultFlag") Integer defaultFlag);
}