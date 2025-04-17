package com.engistech.gateway.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.engistech.gateway.entity.EcallEndHistoryEntity;

public interface EcallEndHistoryRepository extends JpaRepository<EcallEndHistoryEntity, String> {
    List<EcallEndHistoryEntity> findBySessionIdAndCallType(String sessionId, String callType);
}