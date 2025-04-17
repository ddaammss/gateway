package com.engistech.gateway.service;

import java.time.LocalDateTime;

import com.engistech.gateway.entity.*;

public interface MqttService {

    <T> void insertPayload(String topic, T payload);

    // Provisioning 설정 관련
    ProvisioningFinalEntity getProvisioningFinalByVin(String vin);
    ProvisioningFinalEntity insertProvisioningFinal(String vin);
    ProvisioningHistoryEntity insertProvisioningHistory(String vin);
    ProvisioningFinalEntity getProvisioningFinalByFlag(int flag);

    // DHC Interval 설정 관련
    DhcIntervalFinalEntity getDhcIntervalFinalByVin(String vin);
    DhcIntervalFinalEntity insertDhcIntervalFinal(String vin);
    DhcIntervalHistoryEntity insertDhcIntervalHistory(String vin);

    // 콜종료 확인
    EcallEndHistoryEntity getEcallEnd(String sessionId, String callType);

    // 서비스 송/수신 이력 로그
    SvcMainLogEntity insertSvcMainLog(SvcMainLogEntity entity);
    Long getTeleSvcMainIdBySessionId(String sessionId);
    void updateStatusAndTime(String statusCode, LocalDateTime statusTime, String sessionId);
    SvcDetailLogEntity insertSvcDetailLog(SvcDetailLogEntity entity);

    // GPS Data 수신 이력 저장
    SvcHistoryEntity insertSvcHistory(String vin, Integer messageService);

    // 프리셋 default 설정
    void insertDefaultPreset(String mode, String vin);
}
