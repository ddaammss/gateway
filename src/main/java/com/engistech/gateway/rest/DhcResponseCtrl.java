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

import com.engistech.gateway.model.common.CommonHeader;
import com.engistech.gateway.model.common.MessageInfo;
import com.engistech.gateway.model.common.CommonParam;
import com.engistech.gateway.model.common.UserProperties;
import com.engistech.gateway.model.dhc.DhcInterval;
import com.engistech.gateway.model.dhc.DhcResponsePayload;
import com.engistech.gateway.model.dhc.DhcResponsePayloadBody;
import com.engistech.gateway.service.SessionManageService;
import com.engistech.gateway.service.impl.PublisherServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class DhcResponseCtrl {

    @Autowired
    PublisherServiceImpl publisherService;

    @Autowired
    SessionManageService sessionManage;

    // ACN VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    @ResponseBody
    @PostMapping(value = "/wam/24dcm/dhcResponse")
    public ResponseEntity<?> emitDhcEvent(@RequestBody CommonParam responseParam) {

        try {
            // Session ID 및 Correlation ID 확인
            // ----------------------------------------------------------------------------------- //
            String correlationId = sessionManage.getCorrelationId(responseParam.getDeviceId(), MessageInfo.ServiceEnum.DHC.toString());
            String sessionId = sessionManage.getSessionid(responseParam.getDeviceId(), MessageInfo.ServiceEnum.DHC.toString());
            // ----------------------------------------------------------------------------------- //

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {
                
                DhcResponsePayload dhcResponsePayload = new DhcResponsePayload();
        
                // userProperties 설정
                // ------------------------------------------------------------------------------- //
                dhcResponsePayload.getHeader().getUserProperties().setCorrelationId(sessionManage.getCorrelationId(responseParam.getDeviceId(), MessageInfo.ServiceEnum.DHC.toString()));
                dhcResponsePayload.getHeader().getUserProperties().setSessionId(sessionManage.getSessionid(responseParam.getDeviceId(), MessageInfo.ServiceEnum.DHC.toString()));
                // ------------------------------------------------------------------------------- //

                // message 설정
                // ------------------------------------------------------------------------------- //
                dhcResponsePayload.getHeader().getMessage().setType(MessageInfo.TypeEnum.ACK);
                dhcResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.DHC);
                dhcResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.DHC);
                // ------------------------------------------------------------------------------- //
        
                // transmissionTimestampUTC 설정
                dhcResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());
        
                // Payload Body 설정
                // ------------------------------------------------------------------------------- //
                Map<String, Object> additionalData = responseParam.getAdditionalData();

                ObjectMapper objectMapper = new ObjectMapper();
                DhcInterval interval = objectMapper.convertValue(
                    additionalData.get("interval"), 
                    DhcInterval.class
                );

                DhcResponsePayloadBody dhcResponsePayloadBody = new DhcResponsePayloadBody();
                dhcResponsePayloadBody.setInterval(interval);
                dhcResponsePayload.setBody(dhcResponsePayloadBody);
                // ------------------------------------------------------------------------------- //
        
                // Publishing Response to Broker
                String responseTopic = publisherService.receiveDhcResponse(dhcResponsePayload, responseParam.getDeviceId(),responseParam.getEcuId(), responseParam.getAppId());
        
                //Session Manage
                //Request 있어 removeSessionInfo 없음...

                // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
                // ------------------------------------------------------------------------------- //
                HttpHeaders headers = new HttpHeaders();
                headers.add("mqtt-topic", responseTopic);
        
                return ResponseEntity.status(HttpStatus.OK).headers(headers).body(dhcResponsePayload);
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