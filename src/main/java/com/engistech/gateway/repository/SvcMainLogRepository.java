package com.engistech.gateway.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.engistech.gateway.entity.SvcMainLogEntity;

public interface SvcMainLogRepository extends JpaRepository<SvcMainLogEntity, Long> {
       
    @Query("SELECT s.teleSvcMainId FROM SvcMainLogEntity s WHERE s.sessionId = :sessionId")
    List<Long> findTeleSvcMainIdBySessionId(@Param("sessionId") String sessionId);

    @Modifying
    @Transactional
    @Query("UPDATE SvcMainLogEntity s " +
           "SET s.teleSvcStatusCode = :statusCode, " +
           "    s.teleSvcStatusTime = :statusTime " +
           "WHERE s.sessionId = :sessionId")
    int updateStatusAndTimeBySessionId(@Param("statusCode") String statusCode, 
                                       @Param("statusTime") LocalDateTime statusTime, 
                                       @Param("sessionId") String sessionId);
}