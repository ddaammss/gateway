package com.engistech.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.engistech.gateway.entity.SvcDetailLogEntity;

public interface SvcDetailLogRepository extends JpaRepository<SvcDetailLogEntity, Long> {
}