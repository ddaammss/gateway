package com.engistech.gateway.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.engistech.gateway.model.acn.AcnVctResponsePayload;
import com.engistech.gateway.model.common.CommonHeader;
import com.engistech.gateway.model.common.MessageInfo;
import com.engistech.gateway.model.common.CommonParam;
import com.engistech.gateway.model.common.UserProperties;
import com.engistech.gateway.model.rsn.RsnVctResponsePayload;
import com.engistech.gateway.model.sos.SosVctResponsePayload;
import com.engistech.gateway.model.vls.VlsVctResponsePayload;
import com.engistech.gateway.service.SessionManageService;
import com.engistech.gateway.service.impl.PublisherServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class VctResponseCtrl {

    @Autowired
    PublisherServiceImpl publisherService;

    @Autowired
    SessionManageService sessionManage;

    // ACN VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/acnVctResponse")
    public ResponseEntity<?> emitAcnVctEvent(@RequestBody CommonParam responseParam) {

        try {
            // Session ID 및 Correlation ID 확인
            // ----------------------------------------------------------------------------------- //
            String correlationId = sessionManage.getCorrelationId(responseParam.getDeviceId(), MessageInfo.ServiceEnum.ACN.toString());
            String sessionId = sessionManage.getSessionid(responseParam.getDeviceId(), MessageInfo.ServiceEnum.ACN.toString());
            // ----------------------------------------------------------------------------------- //

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {
                
                AcnVctResponsePayload acnVctResponsePayload = new AcnVctResponsePayload();
        
                // userProperties 설정
                // ------------------------------------------------------------------------------- //
                acnVctResponsePayload.getHeader().getUserProperties().setCorrelationId(sessionManage.getCorrelationId(responseParam.getDeviceId(), MessageInfo.ServiceEnum.ACN.toString()));
                acnVctResponsePayload.getHeader().getUserProperties().setSessionId(sessionManage.getSessionid(responseParam.getDeviceId(), MessageInfo.ServiceEnum.ACN.toString()));
                // ------------------------------------------------------------------------------- //
        
                // VCT 응답 확인
                // ------------------------------------------------------------------------------- //
                Map<String, Object> additionalData = responseParam.getAdditionalData();

                String messageTypeStr = (String) additionalData.get("messageType");
                MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.fromValue(messageTypeStr);
                // ------------------------------------------------------------------------------- //

                // message 설정
                // ------------------------------------------------------------------------------- //
                acnVctResponsePayload.getHeader().getMessage().setType(messageType);
                acnVctResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.ACN);
                acnVctResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.CONFIRM_TERMINATION);
                // ------------------------------------------------------------------------------- //
        
                // transmissionTimestampUTC 설정
                acnVctResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());
        
                // Session 삭제 여부 확인할 것!
                // sessionManage.removeSessionInfo(responseParam.getDeviceId());
        
                // Publishing Response to Broker
                String responseTopic = publisherService.receiveAcnVctResponse(acnVctResponsePayload, responseParam.getDeviceId(),responseParam.getEcuId(), responseParam.getAppId());
        
                // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
                // ------------------------------------------------------------------------------- //
                HttpHeaders headers = new HttpHeaders();
                headers.add("mqtt-topic", responseTopic);
        
                return ResponseEntity.status(HttpStatus.OK).headers(headers).body(acnVctResponsePayload);
                // ------------------------------------------------------------------------------- //
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }        
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // SOS VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/sosVctResponse")
    public ResponseEntity<?> emitSosVctEvent(@RequestBody CommonParam responseParam) {

        try {    
            // Session ID 및 Correlation ID 확인
            // ----------------------------------------------------------------------------------- //
            String correlationId = sessionManage.getCorrelationId(responseParam.getDeviceId(), MessageInfo.ServiceEnum.SOS.toString());
            String sessionId = sessionManage.getSessionid(responseParam.getDeviceId(), MessageInfo.ServiceEnum.SOS.toString());
            // ----------------------------------------------------------------------------------- //

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {

                SosVctResponsePayload sosVctResponsePayload = new SosVctResponsePayload();
                
                // userProperties 설정
                // ------------------------------------------------------------------------------- //
                sosVctResponsePayload.getHeader().getUserProperties().setCorrelationId(sessionManage.getCorrelationId(responseParam.getDeviceId(), MessageInfo.ServiceEnum.SOS.toString()));
                sosVctResponsePayload.getHeader().getUserProperties().setSessionId(sessionManage.getSessionid(responseParam.getDeviceId(), MessageInfo.ServiceEnum.SOS.toString()));
                // ------------------------------------------------------------------------------- //
        
                // VCT 응답 확인
                // ------------------------------------------------------------------------------- //
                Map<String, Object> additionalData = responseParam.getAdditionalData();

                String messageTypeStr = (String) additionalData.get("messageType");
                MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.fromValue(messageTypeStr);
                // ------------------------------------------------------------------------------- //

                // message 설정
                // ------------------------------------------------------------------------------- //
                sosVctResponsePayload.getHeader().getMessage().setType(messageType);
                sosVctResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.SOS);
                sosVctResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.CONFIRM_TERMINATION);
                // ------------------------------------------------------------------------------- //
        
                // transmissionTimestampUTC 설정
                sosVctResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());
        
                // Session 삭제 여부 확인할 것!
                // sessionManage.removeSessionInfo(responseParam.getDeviceId());
        
                // Publishing Response to Broker
                String responseTopic = publisherService.receiveSosVctResponse(sosVctResponsePayload, responseParam.getDeviceId(),responseParam.getEcuId(), responseParam.getAppId());
        
                // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
                // ------------------------------------------------------------------------------- //
                HttpHeaders headers = new HttpHeaders();
                headers.add("mqtt-topic", responseTopic);
        
                return ResponseEntity.status(HttpStatus.OK).headers(headers).body(sosVctResponsePayload);
                // ------------------------------------------------------------------------------- //
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }        
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // RSN VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/rsnVctResponse")
    public ResponseEntity<?> emitRsnVctEvent(@RequestBody CommonParam responseParam) {

        try {    
            // Session ID 및 Correlation ID 확인
            // ----------------------------------------------------------------------------------- //
            String correlationId = sessionManage.getCorrelationId(responseParam.getDeviceId(), MessageInfo.ServiceEnum.RSN.toString());
            String sessionId = sessionManage.getSessionid(responseParam.getDeviceId(), MessageInfo.ServiceEnum.RSN.toString());
            // ----------------------------------------------------------------------------------- //

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {

                RsnVctResponsePayload rsnVctResponsePayload = new RsnVctResponsePayload();
                
                // userProperties 설정
                // ------------------------------------------------------------------------------- //
                rsnVctResponsePayload.getHeader().getUserProperties().setCorrelationId(sessionManage.getCorrelationId(responseParam.getDeviceId(), MessageInfo.ServiceEnum.RSN.toString()));
                rsnVctResponsePayload.getHeader().getUserProperties().setSessionId(sessionManage.getSessionid(responseParam.getDeviceId(), MessageInfo.ServiceEnum.RSN.toString()));
                // ------------------------------------------------------------------------------- //
        
                // VCT 응답 확인
                // ------------------------------------------------------------------------------- //
                Map<String, Object> additionalData = responseParam.getAdditionalData();

                String messageTypeStr = (String) additionalData.get("messageType");
                MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.fromValue(messageTypeStr);
                // ------------------------------------------------------------------------------- //

                // message 설정
                // ------------------------------------------------------------------------------- //
                rsnVctResponsePayload.getHeader().getMessage().setType(messageType);
                rsnVctResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.RSN);
                rsnVctResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.CONFIRM_TERMINATION);
                // ------------------------------------------------------------------------------- //
        
                // transmissionTimestampUTC 설정
                rsnVctResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());
        
                // Session 삭제 여부 확인할 것!
                // sessionManage.removeSessionInfo(responseParam.getDeviceId());
        
                // Publishing Response to Broker
                String responseTopic = publisherService.receiveRsnVctResponse(rsnVctResponsePayload, responseParam.getDeviceId(),responseParam.getEcuId(), responseParam.getAppId());
        
                // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
                // ------------------------------------------------------------------------------- //
                HttpHeaders headers = new HttpHeaders();
                headers.add("mqtt-topic", responseTopic);
        
                return ResponseEntity.status(HttpStatus.OK).headers(headers).body(rsnVctResponsePayload);
                // ------------------------------------------------------------------------------- //
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }        
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // VLS VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/vlsVctResponse")
    public ResponseEntity<?> handleVlsVctResponse(@RequestBody CommonParam responseParam) {

        try {    
            // Session ID 및 Correlation ID 확인
            // ----------------------------------------------------------------------------------- //
            String correlationId = sessionManage.getCorrelationId(responseParam.getDeviceId(), MessageInfo.ServiceEnum.VLS.toString());
            String sessionId = sessionManage.getSessionid(responseParam.getDeviceId(), MessageInfo.ServiceEnum.VLS.toString());
            // ----------------------------------------------------------------------------------- //

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {

                VlsVctResponsePayload vlsVctResponsePayload = new VlsVctResponsePayload();
                
                // userProperties 설정
                // ------------------------------------------------------------------------------- //
                vlsVctResponsePayload.getHeader().getUserProperties().setCorrelationId(sessionManage.getCorrelationId(responseParam.getDeviceId(), MessageInfo.ServiceEnum.VLS.toString()));
                vlsVctResponsePayload.getHeader().getUserProperties().setSessionId(sessionManage.getSessionid(responseParam.getDeviceId(), MessageInfo.ServiceEnum.VLS.toString()));
                // ------------------------------------------------------------------------------- //
        
                // VCT 응답 확인
                // ------------------------------------------------------------------------------- //
                Map<String, Object> additionalData = responseParam.getAdditionalData();

                String messageTypeStr = (String) additionalData.get("messageType");
                MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.fromValue(messageTypeStr);
                // ------------------------------------------------------------------------------- //

                // message 설정
                // ------------------------------------------------------------------------------- //
                vlsVctResponsePayload.getHeader().getMessage().setType(messageType);
                vlsVctResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.VLS);
                vlsVctResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.CONFIRM_TERMINATION);
                // ------------------------------------------------------------------------------- //
        
                // transmissionTimestampUTC 설정
                vlsVctResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());
        
                // Session 삭제 여부 확인할 것!
                // sessionManage.removeSessionInfo(responseParam.getDeviceId());
        
                // Publishing Response to Broker
                String topic = publisherService.receiveTrackingVctResponse(vlsVctResponsePayload, responseParam.getDeviceId(),responseParam.getEcuId(), responseParam.getAppId());
        
                // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
                // ------------------------------------------------------------------------------- //
                HttpHeaders headers = new HttpHeaders();
                headers.add("mqtt-topic", topic);
        
                return ResponseEntity.status(HttpStatus.OK).headers(headers).body(vlsVctResponsePayload);
                // ------------------------------------------------------------------------------- //
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }        
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // ------------------------------------------------------------------------------------------------ //
}