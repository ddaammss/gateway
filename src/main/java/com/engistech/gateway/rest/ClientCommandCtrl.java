package com.engistech.gateway.rest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.engistech.gateway.constants.ServiceLogContents;
import com.engistech.gateway.constants.ServiceLogCreators;
import com.engistech.gateway.constants.ServiceLogResultCode;
import com.engistech.gateway.constants.ServiceLogResultMessages;
import com.engistech.gateway.constants.ServiceLogStatusCode;
import com.engistech.gateway.constants.ServiceLogSystems;
import com.engistech.gateway.constants.ServiceLogTypes;
import com.engistech.gateway.entity.ProvisioningFinalEntity;
import com.engistech.gateway.entity.SvcDetailLogEntity;
import com.engistech.gateway.entity.SvcMainLogEntity;
import com.engistech.gateway.model.common.ApiResponse;
import com.engistech.gateway.model.common.CommonHeader;
import com.engistech.gateway.model.common.CommonParam;
import com.engistech.gateway.model.common.MessageInfo;
import com.engistech.gateway.model.common.PhoneNumber;
import com.engistech.gateway.model.common.Provisioning;
import com.engistech.gateway.model.common.RetransmitPayload;
import com.engistech.gateway.model.common.ServiceFlag;
import com.engistech.gateway.model.dhc.DhcCommandPayload;
import com.engistech.gateway.model.provisioning.ProvisioningPayload;
import com.engistech.gateway.model.provisioning.ProvisioningPayloadBody;
import com.engistech.gateway.model.vls.VlsCallSetting;
import com.engistech.gateway.model.vls.VlsStartPayload;
import com.engistech.gateway.model.vls.VlsStartPayloadBody;
import com.engistech.gateway.model.vls.VlsStartReportSetting;
import com.engistech.gateway.model.vls.VlsStopPayload;
import com.engistech.gateway.model.vls.VlsVoiceCallPayload;
import com.engistech.gateway.model.vls.VlsVoiceCallPayloadBody;
import com.engistech.gateway.model.vls.VoicekillPayload;
import com.engistech.gateway.service.MqttService;
import com.engistech.gateway.service.SessionManageService;
import com.engistech.gateway.service.impl.PublisherServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ClientCommandCtrl {

    @Autowired
    PublisherServiceImpl publisherService;

    // Session Manage
    @Autowired
    SessionManageService sessionManage;

    @Autowired
    private MqttService mqttService;

    // ACN Retransmit (CCW Command)
    // -------------------------------------------------------------------------------------------- //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/acnCommand")
    public ResponseEntity<?> acnRetransmit(@RequestBody CommonParam commandParam) {

        return SendRetransmitCommand(commandParam, MessageInfo.ServiceEnum.ACN);
    }
    // -------------------------------------------------------------------------------------------- //


    // SOS Retransmit (CCW Command)
    // -------------------------------------------------------------------------------------------- //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/sosCommand")
    public ResponseEntity<?> sosRetransmit(@RequestBody CommonParam commandParam) {

        return SendRetransmitCommand(commandParam, MessageInfo.ServiceEnum.SOS);
    }
    // -------------------------------------------------------------------------------------------- //


    // RSN Retransmit (CCW Command)
    // -------------------------------------------------------------------------------------------- //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/rsnCommand")
    public ResponseEntity<?> receiveRsnRetransmit(@RequestBody CommonParam commandParam) {

        return SendRetransmitCommand(commandParam, MessageInfo.ServiceEnum.RSN);
    }
    // -------------------------------------------------------------------------------------------- //


    // Retransmit Command 전송
    // -------------------------------------------------------------------------------------------- //
    private ResponseEntity<?> SendRetransmitCommand(CommonParam commandParam, MessageInfo.ServiceEnum serviceType) {

        ApiResponse<Object> response = new ApiResponse<>();

        try {    
            if (commandParam == null || 
                commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty() ||
                commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty() ||
                commandParam.getAppId() == null || commandParam.getAppId().isEmpty()) {
    
                response.setResultCode(HttpStatus.BAD_REQUEST.value());

                if(commandParam == null) {
                    response.setResultMessage("요청 파라미터가 설정되지 않았습니다.");
                }
                else if(commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty()) {
                    response.setResultMessage("deviceId가 설정되지 않았습니다.");
                }
                else if(commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty()) {
                    response.setResultMessage("ecuId가 설정되지 않았습니다.");
                }
                else {
                    response.setResultMessage("appId가 설정되지 않았습니다.");
                }
            
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }
            else if(commandParam.getDeviceId().length() < 17) {

                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage(String.format("deviceId(%s) 설정이 올바르지 않습니다.", commandParam.getDeviceId()));

                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            RetransmitPayload retransmitCommandPayload = new RetransmitPayload();
    
            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            String sessionId = sessionManage.getSessionid(commandParam.getDeviceId(), serviceType.toString());
            retransmitCommandPayload.getHeader().getUserProperties().setSessionId(sessionId);

            retransmitCommandPayload.getHeader().getUserProperties().setCorrelationId(sessionManage.getCorrelationId(commandParam.getDeviceId(), serviceType.toString()));
            retransmitCommandPayload.getHeader().getUserProperties().setSequenceId(sessionManage.createSequenceId());
            // ------------------------------------------------------------------------------- //
    
            // message 설정
            // ------------------------------------------------------------------------------- //
            retransmitCommandPayload.getHeader().getMessage().setType(MessageInfo.TypeEnum.REQUEST);
            retransmitCommandPayload.getHeader().getMessage().setService(serviceType);
            retransmitCommandPayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.RETRANSMIT);
            // ------------------------------------------------------------------------------- //        
            
            // transmissionTimestampUTC 설정
            retransmitCommandPayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            String topic = "";
            String serviceLogType = "";
            String serviceLogContent = "";
            
            // Publishing Command to Broker
            if(serviceType == MessageInfo.ServiceEnum.ACN) {

                serviceLogType = ServiceLogTypes.ACN;
                serviceLogContent = ServiceLogContents.ACN_RETRANSMIT_REQUEST_MQTT_TO_DCM;

                topic = publisherService.receiveAcnRetransmitCommandMessage(retransmitCommandPayload, commandParam.getDeviceId(), commandParam.getEcuId(), commandParam.getAppId());
            }
            else if(serviceType == MessageInfo.ServiceEnum.RSN) {

                serviceLogType = ServiceLogTypes.RSN;
                serviceLogContent = ServiceLogContents.RSN_RETRANSMIT_REQUEST_MQTT_TO_DCM;

                topic = publisherService.receiveRsnRetransmitCommandMessage(retransmitCommandPayload, commandParam.getDeviceId(), commandParam.getEcuId(), commandParam.getAppId());
            }
            else if(serviceType == MessageInfo.ServiceEnum.SOS) {

                serviceLogType = ServiceLogTypes.SOS;
                serviceLogContent = ServiceLogContents.SOS_RETRANSMIT_REQUEST_MQTT_TO_DCM;

                topic = publisherService.receiveSosRetransmitCommandMessage(retransmitCommandPayload, commandParam.getDeviceId(), commandParam.getEcuId(), commandParam.getAppId());
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String strPayload = objectMapper.writeValueAsString(retransmitCommandPayload);

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId == null) {

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    commandParam.getDeviceId(),             // DCM VIN
                    serviceLogType,                         // 서비스 Type
                    Boolean.TRUE.equals(commandParam.getSimFlag()) ? ServiceLogSystems.SIM : ServiceLogSystems.CCW,  // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();

                if(Boolean.TRUE.equals(commandParam.getSimFlag())) {

                    saveServiceDetailLog(
                        strPayload,                                             // Payload
                        teleSvcMainId,                                          // 마스터 로그 식별 ID
                        ServiceLogSystems.SIM,                                  // 출발지
                        ServiceLogSystems.MQTT,                                 // 목적지
                        true,                                        // 결과
                        ServiceLogContents.COMMAND_REQUEST_SIM_TO_MQTT          // 로그내용
                    );
                }
            }
            // ------------------------------------------------------------------------------- //
            
            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                strPayload,                                             // Payload
                teleSvcMainId,                                          // 마스터 로그 식별 ID
                ServiceLogSystems.MQTT,                                 // 출발지
                ServiceLogSystems.DCM,                                  // 목적지
                true,                                        // 결과
                serviceLogContent                                       // 로그내용
            );
            // ------------------------------------------------------------------------------- //
    
            // 브로커로 송신된 Message 화면 표출 목적 (테스트 이후 불필요 시 삭제)
            // ------------------------------------------------------------------------------- //
            HttpHeaders headers = new HttpHeaders();
            headers.add("mqtt-topic", topic);
    
            response.setDcmMessage(retransmitCommandPayload);
        
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(response);
            // ------------------------------------------------------------------------------- //
        } 
        catch (Exception e) {
            log.error("Failed to publish the command to DCM", e);

            response.setResultCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setResultMessage("처리중에 오류가 발생하였습니다. 담당자에게 문의 바랍니다.");

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
        }
    }
    // -------------------------------------------------------------------------------------------- //


    // VLS Start Command From CCW
    // -------------------------------------------------------------------------------------------- //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/vlsCommand/Start")
    public ResponseEntity<?> receiveVlsStartCommand(@RequestBody CommonParam commandParam) {
        
        ApiResponse<Object> response = new ApiResponse<>();

        try {
            if (commandParam == null || 
                commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty() ||
                commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty() ||
                commandParam.getAppId() == null || commandParam.getAppId().isEmpty() ||
                commandParam.getAdditionalData() == null || commandParam.getAdditionalData().isEmpty()) {
    
                response.setResultCode(HttpStatus.BAD_REQUEST.value());

                if(commandParam == null) {
                    response.setResultMessage("요청 파라미터가 설정되지 않았습니다.");
                }
                else if(commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty()) {
                    response.setResultMessage("deviceId가 설정되지 않았습니다.");
                }
                else if(commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty()) {
                    response.setResultMessage("ecuId가 설정되지 않았습니다.");
                }
                else if(commandParam.getAppId() == null || commandParam.getAppId().isEmpty()) {
                    response.setResultMessage("appId가 설정되지 않았습니다.");
                }
                else {
                    response.setResultMessage("reportSetting이 설정되지 않았습니다.");
                }
            
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }
            else if(commandParam.getDeviceId().length() < 17) {

                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage(String.format("deviceId(%s) 설정이 올바르지 않습니다.", commandParam.getDeviceId()));

                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            VlsStartPayload vlsStartCommandPayload = new VlsStartPayload();

            // userProperties 설정 (새로 생성)
            // ------------------------------------------------------------------------------- //
            String sessionId = sessionManage.createUUIDString();
            vlsStartCommandPayload.getHeader().getUserProperties().setSessionId(sessionId);

            vlsStartCommandPayload.getHeader().getUserProperties().setCorrelationId(sessionManage.createUUIDString());
            vlsStartCommandPayload.getHeader().getUserProperties().setSequenceId(sessionManage.createSequenceId());
            // ------------------------------------------------------------------------------- //

            // Session 정보 저장
            // ------------------------------------------------------------------------------- //
            sessionManage.setCorrelationId(commandParam.getDeviceId(), MessageInfo.ServiceEnum.VLS.toString(), vlsStartCommandPayload.getHeader().getUserProperties().getCorrelationId());
            sessionManage.setSessionId(commandParam.getDeviceId(), MessageInfo.ServiceEnum.VLS.toString(), vlsStartCommandPayload.getHeader().getUserProperties().getSessionId());
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            vlsStartCommandPayload.getHeader().getMessage().setType(MessageInfo.TypeEnum.REQUEST);
            vlsStartCommandPayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.VLS);
            vlsStartCommandPayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.START);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            vlsStartCommandPayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());
            
            // Payload Body 설정
            // ------------------------------------------------------------------------------- //
            Map<String, Object> additionalData = commandParam.getAdditionalData();

            if (!additionalData.containsKey("reportSetting")) {
                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage("reportSetting 키가 설정되지 않았습니다.");

                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            Object reportSettingRaw = additionalData.get("reportSetting");
            if (reportSettingRaw == null || 
                (reportSettingRaw instanceof Map && ((Map<?, ?>) reportSettingRaw).isEmpty())) {
                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage("reportSetting 값이 설정되지 않았습니다.");
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            VlsStartReportSetting reportSetting = objectMapper.convertValue(
                reportSettingRaw, 
                VlsStartReportSetting.class
            );

            VlsStartPayloadBody vlsStartCommandPayloadBody = new VlsStartPayloadBody();
            vlsStartCommandPayloadBody.setReportSetting(reportSetting);
            vlsStartCommandPayload.setBody(vlsStartCommandPayloadBody);
            // ------------------------------------------------------------------------------- //

            String strPayload = objectMapper.writeValueAsString(vlsStartCommandPayload);

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId == null) {

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    commandParam.getDeviceId(),                 // DCM VIN
                    ServiceLogTypes.VLS,                        // 서비스 Type
                    Boolean.TRUE.equals(commandParam.getSimFlag()) ? ServiceLogSystems.SIM : ServiceLogSystems.CCW,  // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();

                if(Boolean.TRUE.equals(commandParam.getSimFlag())) {

                    saveServiceDetailLog(
                        strPayload,                                             // Payload
                        teleSvcMainId,                                          // 마스터 로그 식별 ID
                        ServiceLogSystems.SIM,                                  // 출발지
                        ServiceLogSystems.MQTT,                                 // 목적지
                        true,                                        // 결과
                        ServiceLogContents.COMMAND_REQUEST_SIM_TO_MQTT          // 로그내용
                    );
                }
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                strPayload,                                             // Payload
                teleSvcMainId,                                          // 마스터 로그 식별 ID
                ServiceLogSystems.MQTT,                                 // 출발지
                ServiceLogSystems.DCM,                                  // 목적지
                true,                                        // 결과
                ServiceLogContents.VLS_START_REQUEST_SEND_MQTT_TO_DCM   // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Publishing Command to Broker
            String topic = publisherService.receiveVlsStartCommandMessage(vlsStartCommandPayload, commandParam.getDeviceId(), commandParam.getEcuId(), commandParam.getAppId());

            // 브로커로 송신된 Message 화면 표출 목적 (테스트 이후 불필요 시 삭제)
            // ------------------------------------------------------------------------------- //
            HttpHeaders headers = new HttpHeaders();
            headers.add("mqtt-topic", topic);

            response.setDcmMessage(vlsStartCommandPayload);
        
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(response);
            // ------------------------------------------------------------------------------- //
        } 
        catch (Exception e) {
            log.error("Failed to publish the command to DCM", e);

            response.setResultCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setResultMessage("처리중에 오류가 발생하였습니다. 담당자에게 문의 바랍니다.");

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
        }
    }
    // -------------------------------------------------------------------------------------------- //


    // VLS Stop Command From CCW
    // -------------------------------------------------------------------------------------------- //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/vlsCommand/Stop")
    public ResponseEntity<?> receiveVlsStopCommand(@RequestBody CommonParam commandParam) {

        ApiResponse<Object> response = new ApiResponse<>();

        try { 
            if (commandParam == null || 
                commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty() ||
                commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty() ||
                commandParam.getAppId() == null || commandParam.getAppId().isEmpty()) {
    
                response.setResultCode(HttpStatus.BAD_REQUEST.value());

                if(commandParam == null) {
                    response.setResultMessage("요청 파라미터가 설정되지 않았습니다.");
                }
                else if(commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty()) {
                    response.setResultMessage("deviceId가 설정되지 않았습니다.");
                }
                else if(commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty()) {
                    response.setResultMessage("ecuId가 설정되지 않았습니다.");
                }
                else {
                    response.setResultMessage("appId가 설정되지 않았습니다.");
                }
            
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }
            else if(commandParam.getDeviceId().length() < 17) {

                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage(String.format("deviceId(%s) 설정이 올바르지 않습니다.", commandParam.getDeviceId()));

                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            VlsStopPayload vlsStopCommandPayload = new VlsStopPayload();
            
            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            // Session ID 유지
            String sessionId = sessionManage.getSessionid(commandParam.getDeviceId(), MessageInfo.ServiceEnum.VLS.toString());
            vlsStopCommandPayload.getHeader().getUserProperties().setSessionId(sessionId);

            vlsStopCommandPayload.getHeader().getUserProperties().setCorrelationId(sessionManage.createUUIDString());
            vlsStopCommandPayload.getHeader().getUserProperties().setSequenceId(sessionManage.createSequenceId());
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            vlsStopCommandPayload.getHeader().getMessage().setType(MessageInfo.TypeEnum.REQUEST);
            vlsStopCommandPayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.VLS);
            vlsStopCommandPayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.STOP);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            vlsStopCommandPayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            ObjectMapper objectMapper = new ObjectMapper();
            String strPayload = objectMapper.writeValueAsString(vlsStopCommandPayload);

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId == null) {

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    commandParam.getDeviceId(),             // DCM VIN
                    ServiceLogTypes.VLS,                    // 서비스 Type
                    Boolean.TRUE.equals(commandParam.getSimFlag()) ? ServiceLogSystems.SIM : ServiceLogSystems.CCW,  // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();

                if(Boolean.TRUE.equals(commandParam.getSimFlag())) {

                    saveServiceDetailLog(
                        strPayload,                                             // Payload
                        teleSvcMainId,                                          // 마스터 로그 식별 ID
                        ServiceLogSystems.SIM,                                  // 출발지
                        ServiceLogSystems.MQTT,                                 // 목적지
                        true,                                        // 결과
                        ServiceLogContents.COMMAND_REQUEST_SIM_TO_MQTT          // 로그내용
                    );
                }
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                strPayload,                                             // Payload
                teleSvcMainId,                                          // 마스터 로그 식별 ID
                ServiceLogSystems.MQTT,                                 // 출발지
                ServiceLogSystems.DCM,                                  // 목적지
                true,                                        // 결과
                ServiceLogContents.VLS_STOP_REQUEST_SEND_MQTT_TO_DCM    // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Publishing Command to Broker
            String topic = publisherService.receiveVlsStopCommandMessage(vlsStopCommandPayload, commandParam.getDeviceId(), commandParam.getEcuId(), commandParam.getAppId());

            // 브로커로 송신된 Message 화면 표출 목적 (테스트 이후 불필요 시 삭제)
            // ------------------------------------------------------------------------------- //
            HttpHeaders headers = new HttpHeaders();
            headers.add("mqtt-topic", topic);

            response.setDcmMessage(vlsStopCommandPayload);
        
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(response);
            // ------------------------------------------------------------------------------- //
        } 
        catch (Exception e) {
            log.error("Failed to publish the command to DCM", e);

            response.setResultCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setResultMessage("처리중에 오류가 발생하였습니다. 담당자에게 문의 바랍니다.");

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
        }
    }
    // -------------------------------------------------------------------------------------------- //


    // VLS Voice Call Command From CCW
    // -------------------------------------------------------------------------------------------- //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/vlsCommand/Voicecall")
    public ResponseEntity<?> receiveVlsVoicecallCommand(@RequestBody CommonParam commandParam) {

        ApiResponse<Object> response = new ApiResponse<>();

        try {  
            if (commandParam == null || 
                commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty() ||
                commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty() ||
                commandParam.getAppId() == null || commandParam.getAppId().isEmpty() ||
                commandParam.getAdditionalData() == null || commandParam.getAdditionalData().isEmpty()) {
    
                response.setResultCode(HttpStatus.BAD_REQUEST.value());

                if(commandParam == null) {
                    response.setResultMessage("요청 파라미터가 설정되지 않았습니다.");
                }
                else if(commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty()) {
                    response.setResultMessage("deviceId가 설정되지 않았습니다.");
                }
                else if(commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty()) {
                    response.setResultMessage("ecuId가 설정되지 않았습니다.");
                }
                else if(commandParam.getAppId() == null || commandParam.getAppId().isEmpty()) {
                    response.setResultMessage("appId가 설정되지 않았습니다.");
                }
                else {
                    response.setResultMessage("callSettingHmi가 설정되지 않았습니다.");
                }
            
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }
            else if(commandParam.getDeviceId().length() < 17) {

                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage(String.format("deviceId(%s) 설정이 올바르지 않습니다.", commandParam.getDeviceId()));

                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            VlsVoiceCallPayload vlsVoiceCallCommandPayload = new VlsVoiceCallPayload();

            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            // Session ID 유지
            String sessionId = sessionManage.getSessionid(commandParam.getDeviceId(), MessageInfo.ServiceEnum.VLS.toString());
            vlsVoiceCallCommandPayload.getHeader().getUserProperties().setSessionId(sessionId);

            vlsVoiceCallCommandPayload.getHeader().getUserProperties().setCorrelationId(sessionManage.createUUIDString());
            vlsVoiceCallCommandPayload.getHeader().getUserProperties().setSequenceId(sessionManage.createSequenceId());
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            vlsVoiceCallCommandPayload.getHeader().getMessage().setType(MessageInfo.TypeEnum.REQUEST);
            vlsVoiceCallCommandPayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.VLS);
            vlsVoiceCallCommandPayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.VOICE_CALL);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            vlsVoiceCallCommandPayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            // Payload Body 설정
            // ------------------------------------------------------------------------------- //
            Map<String, Object> additionalData = commandParam.getAdditionalData();

            if (!additionalData.containsKey("callSettingHmi")) {
                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage("callSettingHmi 키가 설정되지 않았습니다.");

                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            String callSettingHmiStr = (String) additionalData.get("callSettingHmi");
            if (callSettingHmiStr == null || callSettingHmiStr.isEmpty()) {
                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage("callSettingHmi 값이 설정되지 않았습니다.");
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            VlsCallSetting.HmiEnum hmiType = VlsCallSetting.HmiEnum.fromValue(callSettingHmiStr);

            VlsVoiceCallPayloadBody vlsVoiceCallPayloadBody = new VlsVoiceCallPayloadBody();
            vlsVoiceCallPayloadBody.getCallSetting().setHmi(hmiType);

            vlsVoiceCallCommandPayload.setBody(vlsVoiceCallPayloadBody);
            // ------------------------------------------------------------------------------- //

            ObjectMapper objectMapper = new ObjectMapper();
            String strPayload = objectMapper.writeValueAsString(vlsVoiceCallCommandPayload);

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId == null) {

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    commandParam.getDeviceId(),             // DCM VIN
                    ServiceLogTypes.VLS,                    // 서비스 Type
                    Boolean.TRUE.equals(commandParam.getSimFlag()) ? ServiceLogSystems.SIM : ServiceLogSystems.CCW,  // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();

                if(Boolean.TRUE.equals(commandParam.getSimFlag())) {

                    saveServiceDetailLog(
                        strPayload,                                             // Payload
                        teleSvcMainId,                                          // 마스터 로그 식별 ID
                        ServiceLogSystems.SIM,                                  // 출발지
                        ServiceLogSystems.MQTT,                                 // 목적지
                        true,                                        // 결과
                        ServiceLogContents.COMMAND_REQUEST_SIM_TO_MQTT          // 로그내용
                    );
                }
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                strPayload,                                                 // Payload
                teleSvcMainId,                                              // 마스터 로그 식별 ID
                ServiceLogSystems.MQTT,                                     // 출발지
                ServiceLogSystems.DCM,                                      // 목적지
                true,                                            // 결과
                ServiceLogContents.VLS_VOICECALL_REQUEST_SEND_MQTT_TO_DCM   // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Publishing Command to Broker
            String topic = publisherService.receiveVlsVoiceCallCommandMessage(vlsVoiceCallCommandPayload, commandParam.getDeviceId(), commandParam.getEcuId(), commandParam.getAppId());

            // 브로커로 송신된 Message 화면 표출 목적 (테스트 이후 불필요 시 삭제)
            // ------------------------------------------------------------------------------- //
            HttpHeaders headers = new HttpHeaders();
            headers.add("mqtt-topic", topic);

            response.setDcmMessage(vlsVoiceCallCommandPayload);
        
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(response);
            // ------------------------------------------------------------------------------- //
        } 
        catch (Exception e) {
            log.error("Failed to publish the command to DCM", e);

            response.setResultCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setResultMessage("처리중에 오류가 발생하였습니다. 담당자에게 문의 바랍니다.");

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
        }
    }
    // -------------------------------------------------------------------------------------------- //


    // Provisioning Command From CCW
    // -------------------------------------------------------------------------------------------- //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/provisioningCommand")
    public ResponseEntity<?> receiveProvisioningCommand(@RequestBody CommonParam commandParam) {

        ApiResponse<Object> response = new ApiResponse<>();

        try {
            // Request Parameter 확인
            // ------------------------------------------------------------------------------- //
            if (commandParam == null || 
                commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty() ||
                commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty() ||
                commandParam.getAppId() == null || commandParam.getAppId().isEmpty()) {
    
                response.setResultCode(HttpStatus.BAD_REQUEST.value());

                if(commandParam == null) {
                    response.setResultMessage("요청 파라미터가 설정되지 않았습니다.");
                }
                else if(commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty()) {
                    response.setResultMessage("deviceId가 설정되지 않았습니다.");
                }
                else if(commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty()) {
                    response.setResultMessage("ecuId가 설정되지 않았습니다.");
                }
                else {
                    response.setResultMessage("appId가 설정되지 않았습니다.");
                }
            
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }
            else if(commandParam.getDeviceId().length() < 17) {

                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage(String.format("deviceId(%s) 설정이 올바르지 않습니다.", commandParam.getDeviceId()));

                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }
            // ------------------------------------------------------------------------------- //

            ProvisioningPayload provisioningCommandPayload = new ProvisioningPayload();

            // Header 설정 (새로 생성)
            // ------------------------------------------------------------------------------- //
            String sessionId = sessionManage.createUUIDString();
            provisioningCommandPayload.getHeader().getUserProperties().setSessionId(sessionId);

            provisioningCommandPayload.getHeader().getUserProperties().setCorrelationId(sessionManage.createUUIDString());
            provisioningCommandPayload.getHeader().getUserProperties().setSequenceId(sessionManage.createSequenceId());
            // ------------------------------------------------------------------------------- //

            // Session Manage 저장
            // ------------------------------------------------------------------------------- //
            sessionManage.setCorrelationId(commandParam.getDeviceId(), MessageInfo.ServiceEnum.PROV.toString(), provisioningCommandPayload.getHeader().getUserProperties().getCorrelationId());
            sessionManage.setSessionId(commandParam.getDeviceId(), MessageInfo.ServiceEnum.PROV.toString(), sessionId);
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            provisioningCommandPayload.getHeader().getMessage().setType(MessageInfo.TypeEnum.REQUEST);
            provisioningCommandPayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.PROV);
            provisioningCommandPayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.PROVISIONING);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            provisioningCommandPayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());
            
            // Payload Body 설정
            // ------------------------------------------------------------------------------- //
            ProvisioningPayloadBody bodySetting = new ProvisioningPayloadBody();

            // 시뮬레이터 요청
            if(Boolean.TRUE.equals(commandParam.getSimFlag())) {

                Map<String, Object> additionalData = commandParam.getAdditionalData();

                ObjectMapper objectMapper = new ObjectMapper();
                Provisioning provisioning = objectMapper.convertValue(
                    additionalData.get("provisioning"), 
                    Provisioning.class
                );

                bodySetting.setProvisioning(provisioning);
            } 
            // OPW 요청 : Provisioning 정보를 DB에서 확인하여 요청 처리
            else {
                ProvisioningFinalEntity provisioningFinalEntity = mqttService.getProvisioningFinalByVin(commandParam.getDeviceId());
    
                if(provisioningFinalEntity == null) {
                    
                    response.setResultMessage("프로비저닝 정보가 설정되어 있지 않습니다.");
    
                    return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(response);
                }
    
                // provisioning > serviceFlags
                List<ServiceFlag> serviceFlags = Arrays.asList(
                    new ServiceFlag(ServiceFlag.ServiceEnum.ACN, ServiceFlag.FlagValueEnum.fromInteger(provisioningFinalEntity.getAcnServiceFlag())),
                    new ServiceFlag(ServiceFlag.ServiceEnum.SOS, ServiceFlag.FlagValueEnum.fromInteger(provisioningFinalEntity.getSosServiceFlag())),
                    new ServiceFlag(ServiceFlag.ServiceEnum.VLS, ServiceFlag.FlagValueEnum.fromInteger(provisioningFinalEntity.getVlsServiceFlag())),
                    new ServiceFlag(ServiceFlag.ServiceEnum.RSN, ServiceFlag.FlagValueEnum.fromInteger(provisioningFinalEntity.getRsnServiceFlag())),
                    new ServiceFlag(ServiceFlag.ServiceEnum.DHC, ServiceFlag.FlagValueEnum.fromInteger(provisioningFinalEntity.getDhcServiceFlag()))
                );
                bodySetting.getProvisioning().setServiceFlags(serviceFlags);
    
                // provisioning > brand
                bodySetting.getProvisioning().setBrand(provisioningFinalEntity.getBrand());
    
                // provisioning > provisioningLanguage
                bodySetting.getProvisioning().setProvisioningLanguage(provisioningFinalEntity.getProvisioningLanguage());
    
                // provisioning > configuration > phoneNumbers
    
                List<PhoneNumber> phoneNumbers = new ArrayList<>();
    
                phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.PRIMARY, provisioningFinalEntity.getAcnPhonePrimary()));
                if (provisioningFinalEntity.getAcnPhoneSecondary() != null && !provisioningFinalEntity.getAcnPhoneSecondary().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.SECONDARY, provisioningFinalEntity.getAcnPhoneSecondary()));
                }
                if (provisioningFinalEntity.getAcnPhoneThird() != null && !provisioningFinalEntity.getAcnPhoneThird().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.THIRD, provisioningFinalEntity.getAcnPhoneThird()));
                }
                if (provisioningFinalEntity.getAcnPhoneFourth() != null && !provisioningFinalEntity.getAcnPhoneFourth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.FOURTH, provisioningFinalEntity.getAcnPhoneFourth()));
                }
                if (provisioningFinalEntity.getAcnPhoneFifth() != null && !provisioningFinalEntity.getAcnPhoneFifth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.FIFTH, provisioningFinalEntity.getAcnPhoneFifth()));
                }
                if (provisioningFinalEntity.getAcnPhoneSixth() != null && !provisioningFinalEntity.getAcnPhoneSixth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.SIXTH, provisioningFinalEntity.getAcnPhoneSixth()));
                }
                if (provisioningFinalEntity.getAcnPhoneSeventh() != null && !provisioningFinalEntity.getAcnPhoneSeventh().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.SEVENTH, provisioningFinalEntity.getAcnPhoneSeventh()));
                }
                if (provisioningFinalEntity.getAcnPhoneEighth() != null && !provisioningFinalEntity.getAcnPhoneEighth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.EIGHTH, provisioningFinalEntity.getAcnPhoneEighth()));
                }
                if (provisioningFinalEntity.getAcnPhoneNinth() != null && !provisioningFinalEntity.getAcnPhoneNinth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.NINTH, provisioningFinalEntity.getAcnPhoneNinth()));
                }
                if (provisioningFinalEntity.getAcnPhoneTenth() != null && !provisioningFinalEntity.getAcnPhoneTenth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.TENTH, provisioningFinalEntity.getAcnPhoneTenth()));
                }
    
                phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.PRIMARY, provisioningFinalEntity.getSosPhonePrimary()));
                if (provisioningFinalEntity.getSosPhoneSecondary() != null && !provisioningFinalEntity.getSosPhoneSecondary().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.SECONDARY, provisioningFinalEntity.getSosPhoneSecondary()));
                }
                if (provisioningFinalEntity.getSosPhoneThird() != null && !provisioningFinalEntity.getSosPhoneThird().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.THIRD, provisioningFinalEntity.getSosPhoneThird()));
                }
                if (provisioningFinalEntity.getSosPhoneFourth() != null && !provisioningFinalEntity.getSosPhoneFourth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.FOURTH, provisioningFinalEntity.getSosPhoneFourth()));
                }
                if (provisioningFinalEntity.getSosPhoneFifth() != null && !provisioningFinalEntity.getSosPhoneFifth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.FIFTH, provisioningFinalEntity.getSosPhoneFifth()));
                }
                if (provisioningFinalEntity.getSosPhoneSixth() != null && !provisioningFinalEntity.getSosPhoneSixth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.SIXTH, provisioningFinalEntity.getSosPhoneSixth()));
                }
                if (provisioningFinalEntity.getSosPhoneSeventh() != null && !provisioningFinalEntity.getSosPhoneSeventh().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.SEVENTH, provisioningFinalEntity.getSosPhoneSeventh()));
                }
                if (provisioningFinalEntity.getSosPhoneEighth() != null && !provisioningFinalEntity.getSosPhoneEighth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.EIGHTH, provisioningFinalEntity.getSosPhoneEighth()));
                }
                if (provisioningFinalEntity.getSosPhoneNinth() != null && !provisioningFinalEntity.getSosPhoneNinth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.NINTH, provisioningFinalEntity.getSosPhoneNinth()));
                }
                if (provisioningFinalEntity.getSosPhoneTenth() != null && !provisioningFinalEntity.getSosPhoneTenth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.TENTH, provisioningFinalEntity.getSosPhoneTenth()));
                }
    
                phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.PRIMARY, provisioningFinalEntity.getRsnPhonePrimary()));
                if (provisioningFinalEntity.getRsnPhoneSecondary() != null && !provisioningFinalEntity.getRsnPhoneSecondary().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.SECONDARY, provisioningFinalEntity.getRsnPhoneSecondary()));
                }
                if (provisioningFinalEntity.getRsnPhoneThird() != null && !provisioningFinalEntity.getRsnPhoneThird().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.THIRD, provisioningFinalEntity.getRsnPhoneThird()));
                }
                if (provisioningFinalEntity.getRsnPhoneFourth() != null && !provisioningFinalEntity.getRsnPhoneFourth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.FOURTH, provisioningFinalEntity.getRsnPhoneFourth()));
                }
                if (provisioningFinalEntity.getRsnPhoneFifth() != null && !provisioningFinalEntity.getRsnPhoneFifth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.FIFTH, provisioningFinalEntity.getRsnPhoneFifth()));
                }
                if (provisioningFinalEntity.getRsnPhoneSixth() != null && !provisioningFinalEntity.getRsnPhoneSixth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.SIXTH, provisioningFinalEntity.getRsnPhoneSixth()));
                }
                if (provisioningFinalEntity.getRsnPhoneSeventh() != null && !provisioningFinalEntity.getRsnPhoneSeventh().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.SEVENTH, provisioningFinalEntity.getRsnPhoneSeventh()));
                }
                if (provisioningFinalEntity.getRsnPhoneEighth() != null && !provisioningFinalEntity.getRsnPhoneEighth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.EIGHTH, provisioningFinalEntity.getRsnPhoneEighth()));
                }
                if (provisioningFinalEntity.getRsnPhoneNinth() != null && !provisioningFinalEntity.getRsnPhoneNinth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.NINTH, provisioningFinalEntity.getRsnPhoneNinth()));
                }
                if (provisioningFinalEntity.getRsnPhoneTenth() != null && !provisioningFinalEntity.getRsnPhoneTenth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.TENTH, provisioningFinalEntity.getRsnPhoneTenth()));
                }
    
                phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.PRIMARY, provisioningFinalEntity.getVlsPhonePrimary()));
                if (provisioningFinalEntity.getVlsPhoneSecondary() != null && !provisioningFinalEntity.getVlsPhoneSecondary().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.SECONDARY, provisioningFinalEntity.getVlsPhoneSecondary()));
                }
                if (provisioningFinalEntity.getVlsPhoneThird() != null && !provisioningFinalEntity.getVlsPhoneThird().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.THIRD, provisioningFinalEntity.getVlsPhoneThird()));
                }
                if (provisioningFinalEntity.getVlsPhoneFourth() != null && !provisioningFinalEntity.getVlsPhoneFourth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.FOURTH, provisioningFinalEntity.getVlsPhoneFourth()));
                }
                if (provisioningFinalEntity.getVlsPhoneFifth() != null && !provisioningFinalEntity.getVlsPhoneFifth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.FIFTH, provisioningFinalEntity.getVlsPhoneFifth()));
                }
                if (provisioningFinalEntity.getVlsPhoneSixth() != null && !provisioningFinalEntity.getVlsPhoneSixth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.SIXTH, provisioningFinalEntity.getVlsPhoneSixth()));
                }
                if (provisioningFinalEntity.getVlsPhoneSeventh() != null && !provisioningFinalEntity.getVlsPhoneSeventh().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.SEVENTH, provisioningFinalEntity.getVlsPhoneSeventh()));
                }
                if (provisioningFinalEntity.getVlsPhoneEighth() != null && !provisioningFinalEntity.getVlsPhoneEighth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.EIGHTH, provisioningFinalEntity.getVlsPhoneEighth()));
                }
                if (provisioningFinalEntity.getVlsPhoneNinth() != null && !provisioningFinalEntity.getVlsPhoneNinth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.NINTH, provisioningFinalEntity.getVlsPhoneNinth()));
                }
                if (provisioningFinalEntity.getVlsPhoneTenth() != null && !provisioningFinalEntity.getVlsPhoneTenth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.TENTH, provisioningFinalEntity.getVlsPhoneTenth()));
                }
    
                phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.PRIMARY, provisioningFinalEntity.getInboundPhonePrimary()));
                if (provisioningFinalEntity.getInboundPhoneSecondary() != null && !provisioningFinalEntity.getInboundPhoneSecondary().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.SECONDARY, provisioningFinalEntity.getInboundPhoneSecondary()));
                }
                if (provisioningFinalEntity.getInboundPhoneThird() != null && !provisioningFinalEntity.getInboundPhoneThird().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.THIRD, provisioningFinalEntity.getInboundPhoneThird()));
                }
                if (provisioningFinalEntity.getInboundPhoneFourth() != null && !provisioningFinalEntity.getInboundPhoneFourth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.FOURTH, provisioningFinalEntity.getInboundPhoneFourth()));
                }
                if (provisioningFinalEntity.getInboundPhoneFifth() != null && !provisioningFinalEntity.getInboundPhoneFifth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.FIFTH, provisioningFinalEntity.getInboundPhoneFifth()));
                }
                if (provisioningFinalEntity.getInboundPhoneSixth() != null && !provisioningFinalEntity.getInboundPhoneSixth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.SIXTH, provisioningFinalEntity.getInboundPhoneSixth()));
                }
                if (provisioningFinalEntity.getInboundPhoneSeventh() != null && !provisioningFinalEntity.getInboundPhoneSeventh().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.SEVENTH, provisioningFinalEntity.getInboundPhoneSeventh()));
                }
                if (provisioningFinalEntity.getInboundPhoneEighth() != null && !provisioningFinalEntity.getInboundPhoneEighth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.EIGHTH, provisioningFinalEntity.getInboundPhoneEighth()));
                }
                if (provisioningFinalEntity.getInboundPhoneNinth() != null && !provisioningFinalEntity.getInboundPhoneNinth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.NINTH, provisioningFinalEntity.getInboundPhoneNinth()));
                }
                if (provisioningFinalEntity.getInboundPhoneTenth() != null && !provisioningFinalEntity.getInboundPhoneTenth().trim().isEmpty()) {
                    phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.TENTH, provisioningFinalEntity.getInboundPhoneTenth()));
                }
               
                bodySetting.getProvisioning().getConfiguration().setPhoneNumbers(phoneNumbers);
    
                // provisioning > configuration > callbackStandByTimer
                bodySetting.getProvisioning().getConfiguration().setCallbackStandByTimer(provisioningFinalEntity.getCallbackStandbyTimer());
    
                // provisioning > configuration > sosCancelTimer
                bodySetting.getProvisioning().getConfiguration().setSosCancelTimer(provisioningFinalEntity.getSosCancelTimer());
            }

            provisioningCommandPayload.setBody(bodySetting);
            // ------------------------------------------------------------------------------- //

            ObjectMapper objectMapper = new ObjectMapper();
            String strPayload = objectMapper.writeValueAsString(provisioningCommandPayload);

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId == null) {

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    commandParam.getDeviceId(),                 // DCM VIN
                    ServiceLogTypes.PROV,                       // 서비스 Type
                    Boolean.TRUE.equals(commandParam.getSimFlag()) ? ServiceLogSystems.SIM : ServiceLogSystems.OPW,  // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();

                if(Boolean.TRUE.equals(commandParam.getSimFlag())) {

                    saveServiceDetailLog(
                        strPayload,                                             // Payload
                        teleSvcMainId,                                          // 마스터 로그 식별 ID
                        ServiceLogSystems.SIM,                                  // 출발지
                        ServiceLogSystems.MQTT,                                 // 목적지
                        true,                                        // 결과
                        ServiceLogContents.COMMAND_REQUEST_SIM_TO_MQTT          // 로그내용
                    );
                }
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                strPayload,                                         // Payload
                teleSvcMainId,                                      // 마스터 로그 식별 ID
                ServiceLogSystems.MQTT,                             // 출발지
                ServiceLogSystems.DCM,                              // 목적지
                true,                                    // 결과
                ServiceLogContents.PROVISIONING_SEND_MQTT_TO_DCM    // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Publishing Command to Broker
            String topic = publisherService.receiveProvisioningCommandMessage(provisioningCommandPayload, commandParam.getDeviceId(), commandParam.getEcuId(), commandParam.getAppId());

            // 브로커로 송신된 Message 화면 표출 목적 (테스트 이후 불필요 시 삭제)
            // ------------------------------------------------------------------------------- //
            HttpHeaders headers = new HttpHeaders();
            headers.add("mqtt-topic", topic);

            response.setDcmMessage(provisioningCommandPayload);
        
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(response);
            // ------------------------------------------------------------------------------- //
        } 
        catch (Exception e) {
            log.error("Failed to publish the command to DCM", e);

            response.setResultCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setResultMessage("처리중에 오류가 발생하였습니다. 담당자에게 문의 바랍니다.");

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
        }
    }
    // -------------------------------------------------------------------------------------------- //


    // Cust Provisioning Command From CCW
    // -------------------------------------------------------------------------------------------- //
    /*
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/custProvisioningConfiguration")
    public ResponseEntity<?> custProvisioning(@RequestBody CommonParam commandParam) {

        try {
            CustProvisioningPayload custProvisioningConfigurationPayload = new CustProvisioningPayload();

            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            custProvisioningConfigurationPayload.getHeader().getUserProperties().setCorrelationId(sessionManage.getCorrelationId(commandParam.getDeviceId(), MessageInfo.ServiceEnum.CUST.toString()));
            custProvisioningConfigurationPayload.getHeader().getUserProperties().setSessionId(sessionManage.getSessionid(commandParam.getDeviceId(), MessageInfo.ServiceEnum.CUST.toString()));
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            custProvisioningConfigurationPayload.getHeader().getMessage().setType(MessageInfo.TypeEnum.ACK);
            custProvisioningConfigurationPayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.CUST);
            custProvisioningConfigurationPayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.CUST);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            custProvisioningConfigurationPayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            // Payload Body 설정
            // ------------------------------------------------------------------------------- //
            Map<String, Object> additionalData = commandParam.getAdditionalData();

            ObjectMapper objectMapper = new ObjectMapper();
            CustProvisioningPayloadBody bodySetting = objectMapper.convertValue(
                additionalData.get("bodySetting"), 
                CustProvisioningPayloadBody.class
            );

            custProvisioningConfigurationPayload.setBody(bodySetting);
            // ------------------------------------------------------------------------------- //

            // Publishing Command to Broker
            String topic = publisherService.receiveCustProvisioningConfiguration(custProvisioningConfigurationPayload, commandParam.getDeviceId(), commandParam.getEcuId(), commandParam.getAppId());

            // 브로커로 송신된 Message 화면 표출 목적 (테스트 이후 불필요 시 삭제)
            // ------------------------------------------------------------------------------- //
            HttpHeaders headers = new HttpHeaders();
            headers.add("mqtt-topic", topic);

            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(custProvisioningConfigurationPayload);
            // ------------------------------------------------------------------------------- //
        } 
        catch (Exception e) {
            log.error("Failed to publish the command to DCM", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    */
    // -------------------------------------------------------------------------------------------- //


    // DHC Command From TSP
    // -------------------------------------------------------------------------------------------- //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/dhcCommand")
    public ResponseEntity<?> receiveDhcCommand(@RequestBody CommonParam commandParam) {

        ApiResponse<Object> response = new ApiResponse<>();

        try {
            if (commandParam == null || 
                commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty() ||
                commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty() ||
                commandParam.getAppId() == null || commandParam.getAppId().isEmpty()) {
    
                response.setResultCode(HttpStatus.BAD_REQUEST.value());

                if(commandParam == null) {
                    response.setResultMessage("요청 파라미터가 설정되지 않았습니다.");
                }
                else if(commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty()) {
                    response.setResultMessage("deviceId가 설정되지 않았습니다.");
                }
                else if(commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty()) {
                    response.setResultMessage("ecuId가 설정되지 않았습니다.");
                }
                else {
                    response.setResultMessage("appId가 설정되지 않았습니다.");
                }
            
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }
            else if(commandParam.getDeviceId().length() < 17) {

                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage(String.format("deviceId(%s) 설정이 올바르지 않습니다.", commandParam.getDeviceId()));

                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            DhcCommandPayload dhcCommandPayload = new DhcCommandPayload();
            CommonHeader header = new CommonHeader();     

            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            // dhcCommand의 경우 새로운 SessionId, CorrelationId 생성으로 변경
            //String sessionId = sessionManage.getSessionid(commandParam.getDeviceId(), MessageInfo.ServiceEnum.DHC.toString());
            //header.getUserProperties().setSessionId(sessionId);
            //header.getUserProperties().setCorrelationId(sessionManage.getCorrelationId(commandParam.getDeviceId(), MessageInfo.ServiceEnum.DHC.toString()));
            String sessionId = sessionManage.createUUIDString();
            header.getUserProperties().setSessionId(sessionId);
            header.getUserProperties().setCorrelationId(sessionManage.createUUIDString());
            sessionManage.setSessionId(commandParam.getDeviceId(), MessageInfo.ServiceEnum.DHC.toString(), sessionId); //sessionId 저장

            String correlationId = sessionManage.createUUIDString();
            header.getUserProperties().setCorrelationId(correlationId);
            sessionManage.setCorrelationId(commandParam.getDeviceId(), MessageInfo.ServiceEnum.DHC.toString(), correlationId); //corelationId 저장

            header.getUserProperties().setSequenceId(sessionManage.createSequenceId());
            // ------------------------------------------------------------------------------- //   

            // message 설정
            // ------------------------------------------------------------------------------- //
            header.getMessage().setType(MessageInfo.TypeEnum.REQUEST);
            header.getMessage().setService(MessageInfo.ServiceEnum.DHC);
            header.getMessage().setOperation(MessageInfo.OperationEnum.DHC);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            header.setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            // Header 설정
            dhcCommandPayload.setHeader(header);

            ObjectMapper objectMapper = new ObjectMapper();
            String strPayload = objectMapper.writeValueAsString(dhcCommandPayload);

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);
            
            if(teleSvcMainId == null) {

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    commandParam.getDeviceId(),             // DCM VIN
                    ServiceLogTypes.DHC_CMD,                // 서비스 Type
                    Boolean.TRUE.equals(commandParam.getSimFlag()) ? ServiceLogSystems.SIM : ServiceLogSystems.OPW,  // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();

                if(Boolean.TRUE.equals(commandParam.getSimFlag())) {

                    saveServiceDetailLog(
                        strPayload,                                             // Payload
                        teleSvcMainId,                                          // 마스터 로그 식별 ID
                        ServiceLogSystems.SIM,                                  // 출발지
                        ServiceLogSystems.MQTT,                                 // 목적지
                        true,                                        // 결과
                        ServiceLogContents.COMMAND_REQUEST_SIM_TO_MQTT          // 로그내용
                    );
                }
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                strPayload,                                                // Payload
                teleSvcMainId,                                              // 마스터 로그 식별 ID
                ServiceLogSystems.MQTT,                                     // 출발지
                ServiceLogSystems.DCM,                                      // 목적지
                true,                                            // 결과
                ServiceLogContents.DHC_COMMAND_REQUEST_SEND_MQTT_TO_DCM     // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Publishing Command to Broker
            String topic = publisherService.receiveDhcCommandMessage(dhcCommandPayload, commandParam.getDeviceId(), commandParam.getEcuId(), commandParam.getAppId());

            // Topic 및 Payload 응답
            // ------------------------------------------------------------------------------- //
            HttpHeaders headers = new HttpHeaders();
            headers.add("mqtt-topic", topic);

            response.setDcmMessage(dhcCommandPayload);
        
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(response);
            // ------------------------------------------------------------------------------- //
        } 
        catch (Exception e) {
            log.error("Failed to publish the command to DCM", e);

            response.setResultCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setResultMessage("처리중에 오류가 발생하였습니다. 담당자에게 문의 바랍니다.");

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
        }
    }
    // -------------------------------------------------------------------------------------------- //


    // Voice Kill Command From TSP
    // -------------------------------------------------------------------------------------------- //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/voiceKillCommand")
    public ResponseEntity<?> receiveVoiceKillCommand(@RequestBody CommonParam commandParam) {

        ApiResponse<Object> response = new ApiResponse<>();

        try {
            if (commandParam == null || 
                commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty() ||
                commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty() ||
                commandParam.getAppId() == null || commandParam.getAppId().isEmpty() ||
                commandParam.getAdditionalData() == null || commandParam.getAdditionalData().isEmpty()) {
    
                response.setResultCode(HttpStatus.BAD_REQUEST.value());

                if(commandParam == null) {
                    response.setResultMessage("요청 파라미터가 설정되지 않았습니다.");
                }
                else if(commandParam.getDeviceId() == null || commandParam.getDeviceId().isEmpty()) {
                    response.setResultMessage("deviceId가 설정되지 않았습니다.");
                }
                else if(commandParam.getEcuId() == null || commandParam.getEcuId().isEmpty()) {
                    response.setResultMessage("ecuId가 설정되지 않았습니다.");
                }
                else if(commandParam.getAppId() == null || commandParam.getAppId().isEmpty()) {
                    response.setResultMessage("appId가 설정되지 않았습니다.");
                }
                else {
                    response.setResultMessage("messageService가 설정되지 않았습니다.");
                }
            
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }
            else if(commandParam.getDeviceId().length() < 17) {

                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage(String.format("deviceId(%s) 설정이 올바르지 않습니다.", commandParam.getDeviceId()));

                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            VoicekillPayload voicekillCommandPayload = new VoicekillPayload();
            CommonHeader header = new CommonHeader();

            // Service Name 확인
            // ------------------------------------------------------------------------------- //
            Map<String, Object> additionalData = commandParam.getAdditionalData();

            if (!additionalData.containsKey("messageService")) {
                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage("messageService 키가 설정되지 않았습니다.");

                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            String messageServiceStr = (String) additionalData.get("messageService");
            if (messageServiceStr == null || messageServiceStr.isEmpty()) {
                response.setResultCode(HttpStatus.BAD_REQUEST.value());
                response.setResultMessage("messageService 값이 설정되지 않았습니다.");
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
            }

            MessageInfo.ServiceEnum messageService = MessageInfo.ServiceEnum.fromValue(messageServiceStr);
            // ------------------------------------------------------------------------------- //
            
            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            String sessionId = sessionManage.getSessionid(commandParam.getDeviceId(), messageService.toString());
            header.getUserProperties().setSessionId(sessionId);

            header.getUserProperties().setCorrelationId(sessionManage.getCorrelationId(commandParam.getDeviceId(), messageService.toString()));
            header.getUserProperties().setSequenceId(sessionManage.createSequenceId());
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            header.getMessage().setType(MessageInfo.TypeEnum.REQUEST);
            header.getMessage().setService(messageService);
            header.getMessage().setOperation(MessageInfo.OperationEnum.VOICE_KILL);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            header.setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());       

            // Header 설정
            voicekillCommandPayload.setHeader(header);

            ObjectMapper objectMapper = new ObjectMapper();
            String strPayload = objectMapper.writeValueAsString(voicekillCommandPayload);

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            String serviceLogType = "";

            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId == null) {

                if(messageService == MessageInfo.ServiceEnum.ACN) {
                    serviceLogType = ServiceLogTypes.ACN;
                }
                else if(messageService == MessageInfo.ServiceEnum.RSN) {
                    serviceLogType = ServiceLogTypes.RSN;
                }
                else if(messageService == MessageInfo.ServiceEnum.SOS) {
                    serviceLogType = ServiceLogTypes.SOS;
                }

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    commandParam.getDeviceId(),                 // DCM VIN
                    serviceLogType,                             // 서비스 Type
                    Boolean.TRUE.equals(commandParam.getSimFlag()) ? ServiceLogSystems.SIM : ServiceLogSystems.CCW,  // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();

                if(Boolean.TRUE.equals(commandParam.getSimFlag())) {

                    saveServiceDetailLog(
                        strPayload,                                             // Payload
                        teleSvcMainId,                                          // 마스터 로그 식별 ID
                        ServiceLogSystems.SIM,                                  // 출발지
                        ServiceLogSystems.MQTT,                                 // 목적지
                        true,                                        // 결과
                        ServiceLogContents.COMMAND_REQUEST_SIM_TO_MQTT          // 로그내용
                    );
                }
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                strPayload,                                         // Payload
                teleSvcMainId,                                      // 마스터 로그 식별 ID
                ServiceLogSystems.MQTT,                             // 출발지
                ServiceLogSystems.DCM,                              // 목적지
                true,                                    // 결과
                ServiceLogContents.VOICEKILL_REQUEST_MQTT_TO_DCM    // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Publishing Command to Broker
            String topic = publisherService.receiveVoiceKillCommandMessage(voicekillCommandPayload, commandParam.getDeviceId(), commandParam.getEcuId(), commandParam.getAppId());

            // Topic 및 Payload 응답
            // ------------------------------------------------------------------------------- //
            HttpHeaders headers = new HttpHeaders();
            headers.add("mqtt-topic", topic);

            response.setDcmMessage(voicekillCommandPayload);
        
            return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(response);
            // ------------------------------------------------------------------------------- //
        } 
        catch (Exception e) {
            log.error("Failed to publish the command to DCM", e);

            response.setResultCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setResultMessage("Failed to publish the command to DCM.");

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
        }
    }
    // -------------------------------------------------------------------------------------------- //


    // 차량연동서비스 마스터 로그 저장
    // -------------------------------------------------------------------------------------------- //
    private SvcMainLogEntity saveServiceMainLog(String deviceId, String serviceLogType, String requestSource, String sessionId) {

        SvcMainLogEntity svcMainLogEntity = new SvcMainLogEntity();

        try {            
            // Session ID
            svcMainLogEntity.setSessionId(sessionId);
    
            // DCM VIN
            svcMainLogEntity.setVin(deviceId);

            // 서비스 Type
            svcMainLogEntity.setTeleSvcType(serviceLogType);
    
            // 서비스 요청 일시
            LocalDateTime curTime = LocalDateTime.now();
            svcMainLogEntity.setTeleSvcTime(curTime);
    
            // 서비스 요청 일시 Offset (UTC 기준)
            svcMainLogEntity.setTeleSvcTimeOffset("9");
            
            // 서비스 상태 코드
            svcMainLogEntity.setTeleSvcStatusCode(ServiceLogStatusCode.SUCCESS);
            
            // 서비스 상태 변경 일시
            svcMainLogEntity.setTeleSvcStatusTime(curTime);
            
            // 요청 소스
            svcMainLogEntity.setRequestSource(requestSource);
    
            svcMainLogEntity = mqttService.insertSvcMainLog(svcMainLogEntity);
        }
        catch (Exception e) {
            log.error("처리 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
        }

        return svcMainLogEntity;
    }
    // -------------------------------------------------------------------------------------------- //


    // 차량연동서비스 상세 로그 저장
    // -------------------------------------------------------------------------------------------- //
    private void saveServiceDetailLog(String strMessage, Long svcMainLogId, String requestSource, String requestDest, boolean resultFlag, String contents) {
            
        SvcDetailLogEntity svcDetailLogEntity = new SvcDetailLogEntity();

        // 마스터 로그 식별 ID
        svcDetailLogEntity.setTeleSvcMainId(svcMainLogId);

        // 출발지
        svcDetailLogEntity.setSource(requestSource);

        // 목적지
        svcDetailLogEntity.setDest(requestDest);

        // payload
        svcDetailLogEntity.setPayload(strMessage);

        if(resultFlag) {

            // 결과 상태값
            svcDetailLogEntity.setResultStatus(ServiceLogStatusCode.SUCCESS);

            // 결과 코드
            svcDetailLogEntity.setResultCode(ServiceLogResultCode.SUCCESS);

            // 결과 메시지
            svcDetailLogEntity.setResultMessage(ServiceLogResultMessages.SUCCESS);
        }
        else {
            // 결과 상태값
            svcDetailLogEntity.setResultStatus(ServiceLogStatusCode.FAILURE);
    
            // 결과 코드
            svcDetailLogEntity.setResultCode(ServiceLogResultCode.FAILURE);
    
            // 결과 메시지
            svcDetailLogEntity.setResultMessage(ServiceLogResultMessages.FAILURE);
        }

        // 등록자
        svcDetailLogEntity.setCreatedBy(ServiceLogCreators.MQTT);

        // 등록일자
        LocalDateTime curTime = LocalDateTime.now();
        svcDetailLogEntity.setCreatedTime(curTime);

        // 로그내용
        svcDetailLogEntity.setContent(contents);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode;
            rootNode = objectMapper.readTree(strMessage);

            JsonNode messageNode = rootNode.path("header").path("message");    
    
            // 메시지 구분
            svcDetailLogEntity.setMessageType(MessageInfo.TypeEnum.fromValue(messageNode.path("type").asText()).ordinal());
    
            // 서비스 구분
            svcDetailLogEntity.setMessageService(MessageInfo.ServiceEnum.fromValue(messageNode.path("service").asText()).ordinal());
    
            // Operation 구분
            svcDetailLogEntity.setMessageOperation(MessageInfo.OperationEnum.fromValue(messageNode.path("operation").asText()).ordinal());
        } 
        catch (JsonProcessingException e) {
            log.error("처리 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
        }

        mqttService.insertSvcDetailLog(svcDetailLogEntity);
    }
    // -------------------------------------------------------------------------------------------- //


    // Provisioning On/Off 일괄 처리
    // -------------------------------------------------------------------------------------------- //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/provisioningServiceFlags")
    public ResponseEntity<?> ProvisioningServiceFlags(@RequestBody JsonNode paramData) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        log.info("ProvisioningServiceFlags Request: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(paramData));

        String ecuId = "DESTSW";
        String appId = "safety";

        // 요청 정보
        // ------------------------------------------------------------------------------- //
        // 요청 구분 (1: On, 2: Off)
        int flag = paramData.at("/dsReq/flag").asInt() == 2 ? 0 : 1;

        // 요청 Transaction 식별 ID
        String transactionId = paramData.at("/dsReq/transactionId").asText();

        // 대상 VIN List
        JsonNode vinList = paramData.at("/dsReq/dsReqList");
        // ------------------------------------------------------------------------------- //

        ProvisioningPayloadBody bodySetting = new ProvisioningPayloadBody();
        ProvisioningPayload provisioningCommandPayload = new ProvisioningPayload();

        // Provisioning Body 설정
        // ------------------------------------------------------------------------------- //
        // provisioning > serviceFlags
        List<ServiceFlag> serviceFlags = Arrays.asList(
            new ServiceFlag(ServiceFlag.ServiceEnum.ACN, ServiceFlag.FlagValueEnum.fromInteger(flag)),
            new ServiceFlag(ServiceFlag.ServiceEnum.SOS, ServiceFlag.FlagValueEnum.fromInteger(flag)),
            new ServiceFlag(ServiceFlag.ServiceEnum.VLS, ServiceFlag.FlagValueEnum.fromInteger(flag)),
            new ServiceFlag(ServiceFlag.ServiceEnum.RSN, ServiceFlag.FlagValueEnum.fromInteger(flag)),
            new ServiceFlag(ServiceFlag.ServiceEnum.DHC, ServiceFlag.FlagValueEnum.fromInteger(flag))
        );
        bodySetting.getProvisioning().setServiceFlags(serviceFlags);

        // Default Provisioning Setting
        ProvisioningFinalEntity provisioningFinalEntity = mqttService.getProvisioningFinalByFlag(1);

        // provisioning > brand
        bodySetting.getProvisioning().setBrand(provisioningFinalEntity.getBrand());

        // provisioning > provisioningLanguage
        bodySetting.getProvisioning().setProvisioningLanguage(provisioningFinalEntity.getProvisioningLanguage());

        // provisioning > configuration > phoneNumbers
        List<PhoneNumber> phoneNumbers = new ArrayList<>();

        // ACN phoneNumbers
        phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.PRIMARY, provisioningFinalEntity.getAcnPhonePrimary()));
        if (provisioningFinalEntity.getAcnPhoneSecondary() != null && !provisioningFinalEntity.getAcnPhoneSecondary().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.SECONDARY, provisioningFinalEntity.getAcnPhoneSecondary()));
        }
        if (provisioningFinalEntity.getAcnPhoneThird() != null && !provisioningFinalEntity.getAcnPhoneThird().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.THIRD, provisioningFinalEntity.getAcnPhoneThird()));
        }
        if (provisioningFinalEntity.getAcnPhoneFourth() != null && !provisioningFinalEntity.getAcnPhoneFourth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.FOURTH, provisioningFinalEntity.getAcnPhoneFourth()));
        }
        if (provisioningFinalEntity.getAcnPhoneFifth() != null && !provisioningFinalEntity.getAcnPhoneFifth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.FIFTH, provisioningFinalEntity.getAcnPhoneFifth()));
        }
        if (provisioningFinalEntity.getAcnPhoneSixth() != null && !provisioningFinalEntity.getAcnPhoneSixth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.SIXTH, provisioningFinalEntity.getAcnPhoneSixth()));
        }
        if (provisioningFinalEntity.getAcnPhoneSeventh() != null && !provisioningFinalEntity.getAcnPhoneSeventh().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.SEVENTH, provisioningFinalEntity.getAcnPhoneSeventh()));
        }
        if (provisioningFinalEntity.getAcnPhoneEighth() != null && !provisioningFinalEntity.getAcnPhoneEighth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.EIGHTH, provisioningFinalEntity.getAcnPhoneEighth()));
        }
        if (provisioningFinalEntity.getAcnPhoneNinth() != null && !provisioningFinalEntity.getAcnPhoneNinth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.NINTH, provisioningFinalEntity.getAcnPhoneNinth()));
        }
        if (provisioningFinalEntity.getAcnPhoneTenth() != null && !provisioningFinalEntity.getAcnPhoneTenth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.TENTH, provisioningFinalEntity.getAcnPhoneTenth()));
        }

        // SOS phoneNumbers
        phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.PRIMARY, provisioningFinalEntity.getSosPhonePrimary()));
        if (provisioningFinalEntity.getSosPhoneSecondary() != null && !provisioningFinalEntity.getSosPhoneSecondary().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.SECONDARY, provisioningFinalEntity.getSosPhoneSecondary()));
        }
        if (provisioningFinalEntity.getSosPhoneThird() != null && !provisioningFinalEntity.getSosPhoneThird().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.THIRD, provisioningFinalEntity.getSosPhoneThird()));
        }
        if (provisioningFinalEntity.getSosPhoneFourth() != null && !provisioningFinalEntity.getSosPhoneFourth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.FOURTH, provisioningFinalEntity.getSosPhoneFourth()));
        }
        if (provisioningFinalEntity.getSosPhoneFifth() != null && !provisioningFinalEntity.getSosPhoneFifth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.FIFTH, provisioningFinalEntity.getSosPhoneFifth()));
        }
        if (provisioningFinalEntity.getSosPhoneSixth() != null && !provisioningFinalEntity.getSosPhoneSixth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.SIXTH, provisioningFinalEntity.getSosPhoneSixth()));
        }
        if (provisioningFinalEntity.getSosPhoneSeventh() != null && !provisioningFinalEntity.getSosPhoneSeventh().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.SEVENTH, provisioningFinalEntity.getSosPhoneSeventh()));
        }
        if (provisioningFinalEntity.getSosPhoneEighth() != null && !provisioningFinalEntity.getSosPhoneEighth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.EIGHTH, provisioningFinalEntity.getSosPhoneEighth()));
        }
        if (provisioningFinalEntity.getSosPhoneNinth() != null && !provisioningFinalEntity.getSosPhoneNinth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.NINTH, provisioningFinalEntity.getSosPhoneNinth()));
        }
        if (provisioningFinalEntity.getSosPhoneTenth() != null && !provisioningFinalEntity.getSosPhoneTenth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.TENTH, provisioningFinalEntity.getSosPhoneTenth()));
        }

        // RSN phoneNumbers
        phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.PRIMARY, provisioningFinalEntity.getRsnPhonePrimary()));
        if (provisioningFinalEntity.getRsnPhoneSecondary() != null && !provisioningFinalEntity.getRsnPhoneSecondary().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.SECONDARY, provisioningFinalEntity.getRsnPhoneSecondary()));
        }
        if (provisioningFinalEntity.getRsnPhoneThird() != null && !provisioningFinalEntity.getRsnPhoneThird().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.THIRD, provisioningFinalEntity.getRsnPhoneThird()));
        }
        if (provisioningFinalEntity.getRsnPhoneFourth() != null && !provisioningFinalEntity.getRsnPhoneFourth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.FOURTH, provisioningFinalEntity.getRsnPhoneFourth()));
        }
        if (provisioningFinalEntity.getRsnPhoneFifth() != null && !provisioningFinalEntity.getRsnPhoneFifth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.FIFTH, provisioningFinalEntity.getRsnPhoneFifth()));
        }
        if (provisioningFinalEntity.getRsnPhoneSixth() != null && !provisioningFinalEntity.getRsnPhoneSixth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.SIXTH, provisioningFinalEntity.getRsnPhoneSixth()));
        }
        if (provisioningFinalEntity.getRsnPhoneSeventh() != null && !provisioningFinalEntity.getRsnPhoneSeventh().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.SEVENTH, provisioningFinalEntity.getRsnPhoneSeventh()));
        }
        if (provisioningFinalEntity.getRsnPhoneEighth() != null && !provisioningFinalEntity.getRsnPhoneEighth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.EIGHTH, provisioningFinalEntity.getRsnPhoneEighth()));
        }
        if (provisioningFinalEntity.getRsnPhoneNinth() != null && !provisioningFinalEntity.getRsnPhoneNinth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.NINTH, provisioningFinalEntity.getRsnPhoneNinth()));
        }
        if (provisioningFinalEntity.getRsnPhoneTenth() != null && !provisioningFinalEntity.getRsnPhoneTenth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.TENTH, provisioningFinalEntity.getRsnPhoneTenth()));
        }

        // VLS phoneNumbers
        phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.PRIMARY, provisioningFinalEntity.getVlsPhonePrimary()));
        if (provisioningFinalEntity.getVlsPhoneSecondary() != null && !provisioningFinalEntity.getVlsPhoneSecondary().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.SECONDARY, provisioningFinalEntity.getVlsPhoneSecondary()));
        }
        if (provisioningFinalEntity.getVlsPhoneThird() != null && !provisioningFinalEntity.getVlsPhoneThird().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.THIRD, provisioningFinalEntity.getVlsPhoneThird()));
        }
        if (provisioningFinalEntity.getVlsPhoneFourth() != null && !provisioningFinalEntity.getVlsPhoneFourth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.FOURTH, provisioningFinalEntity.getVlsPhoneFourth()));
        }
        if (provisioningFinalEntity.getVlsPhoneFifth() != null && !provisioningFinalEntity.getVlsPhoneFifth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.FIFTH, provisioningFinalEntity.getVlsPhoneFifth()));
        }
        if (provisioningFinalEntity.getVlsPhoneSixth() != null && !provisioningFinalEntity.getVlsPhoneSixth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.SIXTH, provisioningFinalEntity.getVlsPhoneSixth()));
        }
        if (provisioningFinalEntity.getVlsPhoneSeventh() != null && !provisioningFinalEntity.getVlsPhoneSeventh().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.SEVENTH, provisioningFinalEntity.getVlsPhoneSeventh()));
        }
        if (provisioningFinalEntity.getVlsPhoneEighth() != null && !provisioningFinalEntity.getVlsPhoneEighth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.EIGHTH, provisioningFinalEntity.getVlsPhoneEighth()));
        }
        if (provisioningFinalEntity.getVlsPhoneNinth() != null && !provisioningFinalEntity.getVlsPhoneNinth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.NINTH, provisioningFinalEntity.getVlsPhoneNinth()));
        }
        if (provisioningFinalEntity.getVlsPhoneTenth() != null && !provisioningFinalEntity.getVlsPhoneTenth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.TENTH, provisioningFinalEntity.getVlsPhoneTenth()));
        }

        // INBOUND phoneNumbers
        phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.PRIMARY, provisioningFinalEntity.getInboundPhonePrimary()));
        if (provisioningFinalEntity.getInboundPhoneSecondary() != null && !provisioningFinalEntity.getInboundPhoneSecondary().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.SECONDARY, provisioningFinalEntity.getInboundPhoneSecondary()));
        }
        if (provisioningFinalEntity.getInboundPhoneThird() != null && !provisioningFinalEntity.getInboundPhoneThird().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.THIRD, provisioningFinalEntity.getInboundPhoneThird()));
        }
        if (provisioningFinalEntity.getInboundPhoneFourth() != null && !provisioningFinalEntity.getInboundPhoneFourth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.FOURTH, provisioningFinalEntity.getInboundPhoneFourth()));
        }
        if (provisioningFinalEntity.getInboundPhoneFifth() != null && !provisioningFinalEntity.getInboundPhoneFifth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.FIFTH, provisioningFinalEntity.getInboundPhoneFifth()));
        }
        if (provisioningFinalEntity.getInboundPhoneSixth() != null && !provisioningFinalEntity.getInboundPhoneSixth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.SIXTH, provisioningFinalEntity.getInboundPhoneSixth()));
        }
        if (provisioningFinalEntity.getInboundPhoneSeventh() != null && !provisioningFinalEntity.getInboundPhoneSeventh().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.SEVENTH, provisioningFinalEntity.getInboundPhoneSeventh()));
        }
        if (provisioningFinalEntity.getInboundPhoneEighth() != null && !provisioningFinalEntity.getInboundPhoneEighth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.EIGHTH, provisioningFinalEntity.getInboundPhoneEighth()));
        }
        if (provisioningFinalEntity.getInboundPhoneNinth() != null && !provisioningFinalEntity.getInboundPhoneNinth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.NINTH, provisioningFinalEntity.getInboundPhoneNinth()));
        }
        if (provisioningFinalEntity.getInboundPhoneTenth() != null && !provisioningFinalEntity.getInboundPhoneTenth().trim().isEmpty()) {
            phoneNumbers.add(new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.TENTH, provisioningFinalEntity.getInboundPhoneTenth()));
        }

        bodySetting.getProvisioning().getConfiguration().setPhoneNumbers(phoneNumbers);

        // provisioning > configuration > callbackStandByTimer
        bodySetting.getProvisioning().getConfiguration().setCallbackStandByTimer(provisioningFinalEntity.getCallbackStandbyTimer());

        // provisioning > configuration > sosCancelTimer
        bodySetting.getProvisioning().getConfiguration().setSosCancelTimer(provisioningFinalEntity.getSosCancelTimer());

        provisioningCommandPayload.setBody(bodySetting);
        // ------------------------------------------------------------------------------- //

        // 요청 VIN별 Provisioning 최종 설정 및 송신 처리
        // ------------------------------------------------------------------------------- //
        List<HashMap<String, Object>> responseList = new ArrayList<HashMap<String, Object>>();

        if (!vinList.isMissingNode() && !vinList.isNull() && vinList.isArray() && vinList.size() > 0) {

            for (JsonNode item : vinList) {
                
                // VIN 확인
                // ------------------------------------------------------------------- //
                if (!item.has("vin")) {
                    log.error("VIN 코드 Not Set!");
                    continue;
                }
                
                if (!item.get("vin").isTextual()) {
                    log.error("VIN 코드 설정 오류!");
                    continue;
                }

                String vin = item.get("vin").asText().trim();

                if(vin.isEmpty()) {
                    log.error("VIN 코드 Empty!");
                    continue;
                }
                // ------------------------------------------------------------------- //

                // Header 설정 (새로 생성)
                // ------------------------------------------------------------------- //
                String sessionId = sessionManage.createUUIDString();
                provisioningCommandPayload.getHeader().getUserProperties().setSessionId(sessionId);

                provisioningCommandPayload.getHeader().getUserProperties().setCorrelationId(sessionManage.createUUIDString());
                provisioningCommandPayload.getHeader().getUserProperties().setSequenceId(sessionManage.createSequenceId());
                // ------------------------------------------------------------------- //

                // Session Manage 저장
                // ------------------------------------------------------------------- //
                sessionManage.setCorrelationId(vin, MessageInfo.ServiceEnum.PROV.toString(), provisioningCommandPayload.getHeader().getUserProperties().getCorrelationId());
                sessionManage.setSessionId(vin, MessageInfo.ServiceEnum.PROV.toString(), sessionId);
                // ------------------------------------------------------------------- //

                // message 설정
                // ------------------------------------------------------------------- //
                provisioningCommandPayload.getHeader().getMessage().setType(MessageInfo.TypeEnum.REQUEST);
                provisioningCommandPayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.PROV);
                provisioningCommandPayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.PROVISIONING);
                // ------------------------------------------------------------------- //

                // transmissionTimestampUTC 설정
                provisioningCommandPayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

                String strPayload = objectMapper.writeValueAsString(provisioningCommandPayload);

                // Service Transaction Main Log 기록
                // ------------------------------------------------------------------- //
                Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

                if(teleSvcMainId == null) {

                    SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                        vin,                 // DCM VIN
                        ServiceLogTypes.PROV,                       // 서비스 Type
                        ServiceLogSystems.OBG,                      // 요청 소스
                        sessionId
                    );

                    teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();

                    saveServiceDetailLog(
                        strPayload,                                         // Payload
                        teleSvcMainId,                                      // 마스터 로그 식별 ID
                        ServiceLogSystems.OBG,                              // 출발지
                        ServiceLogSystems.MQTT,                             // 목적지
                        true,                                    // 결과
                        ServiceLogContents.PROVISIONING_REQUEST_OBG_TO_MQTT // 로그내용
                    );
                }
                // ------------------------------------------------------------------- //

                // Service Transaction Detail Log 기록
                // ------------------------------------------------------------------- //
                saveServiceDetailLog(
                    strPayload,                                         // Payload
                    teleSvcMainId,                                      // 마스터 로그 식별 ID
                    ServiceLogSystems.MQTT,                             // 출발지
                    ServiceLogSystems.DCM,                              // 목적지
                    true,                                    // 결과
                    ServiceLogContents.PROVISIONING_SEND_MQTT_TO_DCM    // 로그내용
                );
                // ------------------------------------------------------------------- //

                // Publishing Command to Broker
                publisherService.receiveProvisioningCommandMessage(provisioningCommandPayload, vin, ecuId, appId);

                // 브로커로 송신된 Message 화면 표출 목적 (테스트 이후 불필요 시 삭제)
                // ------------------------------------------------------------------- //
                HashMap<String, Object> response = new HashMap<String, Object>();

                response.put("vin", vin);
                response.put("transactionId", transactionId);
                response.put("type", provisioningCommandPayload.getHeader().getMessage().getType());

                responseList.add(response);
                // ------------------------------------------------------------------- //
            }
        }
        // ------------------------------------------------------------------------------- //

        return ResponseEntity.status(HttpStatus.OK).body(responseList);
    }
}
