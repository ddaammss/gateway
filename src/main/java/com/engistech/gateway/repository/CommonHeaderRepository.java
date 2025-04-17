package com.engistech.gateway.repository;

import com.engistech.gateway.entity.CommonHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * packageName    : com.engistech.gateway.repository
 * fileName       : CommonHeaderRepository
 * author         : jjj
 * date           : 2024-12-27
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-12-27        jjj       최초 생성
 */
@Repository
public interface CommonHeaderRepository extends JpaRepository<CommonHeaderEntity, Long> {
}
