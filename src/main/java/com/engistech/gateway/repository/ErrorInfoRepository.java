package com.engistech.gateway.repository;

import com.engistech.gateway.entity.ErrorInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *packageName    : com.engistech.gateway.repository
 * fileName       : ErrorInfoRepository
 * author         : jjj
 * date           : 2024-12-27
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-12-27        jjj       최초 생성
 */
@Repository
public interface ErrorInfoRepository extends JpaRepository<ErrorInfoEntity, Long> {
}
