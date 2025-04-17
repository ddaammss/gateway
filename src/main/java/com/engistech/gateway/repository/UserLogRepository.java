package com.engistech.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.engistech.gateway.entity.UserLogEntity;

public interface UserLogRepository extends JpaRepository<UserLogEntity, Long> {
}