package com.engistech.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.engistech.gateway.entity.DhcIntervalHistoryEntity;

public interface DhcIntervalHistoryRepository extends JpaRepository<DhcIntervalHistoryEntity, Integer> {
}