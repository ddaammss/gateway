package com.engistech.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.engistech.gateway.entity.SvcHistoryEntity;

public interface SvcHistoryRepository extends JpaRepository<SvcHistoryEntity, Long> {
}