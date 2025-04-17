package com.engistech.gateway.repository;

import com.engistech.gateway.entity.ImpactDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImpactDataEntityRepository extends JpaRepository<ImpactDataEntity, Integer> {
}