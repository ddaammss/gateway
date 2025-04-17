package com.engistech.gateway.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class SessionManageService {
    //deviceId -> CorrelationId, SessionId
    //private final static Map<String, Map<String, String>> userPropertiesManager = new HashMap<>();

    /* CorrelationId, SessionId 별도 관리.
     * deviceId -> Service ID -> CorrelationId
     * deviceId -> Service ID -> SessionId
    */
    private final static Map<String, Map<String, String>> userPropertiesCorrelation = new HashMap<>();
    private final static Map<String, Map<String, String>> userPropertiesSession = new HashMap<>();

    /*
     * LG DCM 연동 사항
     * 
     * Service (SOS, ACN...) 실행중 다른 Service 발생 고려해야함.
     * 
     * TSP Response case : 
     * correlationId : Topic 마다 변경됨, Response 시 저장값 사용, Retransmit 시 저장된 값 사용
     * sessionId : 하나의 Service 시작부터 종료까지 유지, Response 시 저장값 사용, Retransmit 수신 후 DCM SessionId 변경함
     * 
     * TSP Request case : VLS, Provisioning
     * correlationId : Request 시 마다 생성하여 전송
     * sessionId : 첫 Request시 생성하여 유지
     * 
     * 1. No Service running 시 cmd/sos 등 전달 시
     * -> errorPayload : DCM sessionId="", correlationId=NEW
     * 
     * 2. ACN running and receive SOS request
     * -> errorPayload : DCM sessionId=ACN sessionId, correlationId=전송된 것
     * 
     * 
     * 
     */

    public void setCorrelationId( String deviceId, String service, String correlationId) {
        if (StringUtils.hasText(correlationId)) {
            putUserPropertiesCorrelation(deviceId, service, correlationId);
        }
    }

    public void setSessionId(String deviceId, String service, String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            putUserPropertiesSession(deviceId, service, sessionId);
        }
    }

    public String getCorrelationId(String deviceId, String service) {
        String value = getUserPropertiesCorrelation(deviceId, service);
        return value;
    }

    public String getSessionid(String deviceId, String service) {
        String value = getUserPropertiesSession(deviceId, service);
        return value;
    }

    public void removeSessionInfo(String deviceId) {
        // if (!deviceId.isEmpty()) {
        //     removeUserPropertiesManager(deviceId, service);
        // }
    }
    // private void removeUserPropertiesCorrelation(String deviceId, String service) {
    //     if (userPropertiesCorrelation.containsKey(deviceId)) {
    //         userPropertiesCorrelation.remove(service);
    //     }
    // }

    // private void removeUserPropertiesSession(String deviceId, String service) {
    //     if (userPropertiesSession.containsKey(deviceId)) {
    //         userPropertiesSession.remove(service);
    //     }
    // }

    
    /*
     * private 함수 DB 적용 필요
     */
    private void putUserPropertiesCorrelation(String deviceId, String service, String value) {
        userPropertiesCorrelation.computeIfAbsent(deviceId, k -> new HashMap<>()).put(service, value);
    }

    private void putUserPropertiesSession(String deviceId, String service, String value) {
        userPropertiesSession.computeIfAbsent(deviceId, k -> new HashMap<>()).put(service, value);
    }

    private String getUserPropertiesCorrelation(String deviceId, String service) {
        String value = "";

        if (userPropertiesCorrelation.containsKey(deviceId)) {
            value = userPropertiesCorrelation.getOrDefault(deviceId, new HashMap<>()).get(service);
            if (value == null || value.isEmpty()) {
                value = createUUIDString();
                putUserPropertiesCorrelation(deviceId, service, value);
            }
        } else {
            value = createUUIDString();
            putUserPropertiesCorrelation(deviceId, service, value);
        }

        return value;
    }

    private String getUserPropertiesSession(String deviceId, String service) {
        String value = "";

        if (userPropertiesSession.containsKey(deviceId)) {
            value = userPropertiesSession.getOrDefault(deviceId, new HashMap<>()).get(service);
            if (value == null || value.isEmpty()) {
                value = createUUIDString();
                putUserPropertiesSession(deviceId, service, value);
            }
        } else {
            value = createUUIDString();
            putUserPropertiesSession(deviceId, service, value);
        }

        return value;
    }


    public String createUUIDString() {
        //RFC 4122 Version 4 UUID 생성
        return UUID.randomUUID().toString();
    }

    public long createSequenceId() {
        // 현재 시간의 Epoch 타임스탬프 (밀리초 포함)
        long timestamp = System.currentTimeMillis();
        return timestamp;
    }

    public String getCurrentOffsetDateTime() {
        OffsetDateTime currentDateTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX");

        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }
}
