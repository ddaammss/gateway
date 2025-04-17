package com.engistech.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.engistech.gateway.entity.ProvisioningHistoryEntity;

public interface ProvisioningHistoryRepository extends JpaRepository<ProvisioningHistoryEntity, Integer> {
}