package com.engistech.gateway.service;

import com.engistech.gateway.config.ConvertUilts;
import com.engistech.gateway.config.MqttProperties;
import com.engistech.gateway.constants.*;
import com.engistech.gateway.entity.*;
import com.engistech.gateway.model.acn.AcnPayload;
import com.engistech.gateway.model.acn.AcnResponsePayload;
import com.engistech.gateway.model.acn.AcnVctPayload;
import com.engistech.gateway.model.acn.AcnVctResponsePayload;
import com.engistech.gateway.model.common.*;
import com.engistech.gateway.model.dhc.DhcPayload;
import com.engistech.gateway.model.dhc.DhcResponsePayload;
import com.engistech.gateway.model.dhc.DhcResponsePayloadBody;
import com.engistech.gateway.model.provisioning.*;
import com.engistech.gateway.model.rsn.RsnPayload;
import com.engistech.gateway.model.rsn.RsnResponsePayload;
import com.engistech.gateway.model.rsn.RsnVctPayload;
import com.engistech.gateway.model.rsn.RsnVctResponsePayload;
import com.engistech.gateway.model.sos.SosPayload;
import com.engistech.gateway.model.sos.SosResponsePayload;
import com.engistech.gateway.model.sos.SosVctPayload;
import com.engistech.gateway.model.sos.SosVctResponsePayload;
import com.engistech.gateway.model.vls.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.processing.Generated;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Slf4j
@Generated(value = "com.cmm.asyncapi.generator.template.spring", date = "2024-11-01T09:28:17.821Z")
@Service
public class MessageHandlerService {

    // Topic Parameters 추출 위치
    private static final List<Integer> PARAMS_POSITIONS = Arrays.asList(0, 2, 3);

    // 메시지 저장 Queue
    private final BlockingQueue<Message<?>> messageQueue = new LinkedBlockingQueue<>(500);

    private final Map<Pattern, Consumer<Message<?>>> topicHandlers = new HashMap<>();
    private final MqttProperties mqttProperties;
    private final MqttService mqttService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String CCB_URL = System.getenv("CCB_URL");
    private final String CCB_PORT = System.getenv("CCB_PORT");

    @Value("${ccb.uri}")
    private String SPRING_CCB_URI;
    @Value("${ccb.port}")
    private String SPRING_CCB_PORT;
    @Value("${spring.profiles.active}")
    private String PROFILE_ACTIVE;

    @Autowired
    public MessageHandlerService(MqttProperties mqttProperties, MqttService mqttService, ObjectMapper objectMapper) {

        this.mqttProperties = mqttProperties;
        this.mqttService = mqttService;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 메시지 Queue 처리 시작
        startProcessingMessages();

        // Shutdown Hook 등록
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
        }));
    }

    @Autowired
    PublisherService publisherService;

    // Session management
    public Map<String, UserProperties> userPropertiesManager = new HashMap<>(); //지우기
    @Autowired
    SessionManageService sessionManage;

    // Subscribing 대상 토픽 20개에 대해 Handler 등록
    // ------------------------------------------------------------------------------------------------ //
    @PostConstruct
    public void init() {

        mqttProperties.getSubscribe().forEach((key, topic) -> {

            String regex = topic.replace("+", "[^/]+").replace("#", ".*");
            Pattern pattern = Pattern.compile("^" + regex + "$");

            switch (key) {
                case "emitAcnEvent":
                    topicHandlers.put(pattern, this::handleEmitAcnEvent);
                    break;
                case "emitAcnVctEvent":
                    topicHandlers.put(pattern, this::handleEmitAcnVctEvent);
                    break;
                case "emitAcnErrorEvent":
                    topicHandlers.put(pattern, this::handleEmitAcnErrorEvent);
                    break;
                case "emitSosEvent":
                    topicHandlers.put(pattern, this::handleEmitSosEvent);
                    break;
                case "emitSosVctEvent":
                    topicHandlers.put(pattern, this::handleEmitSosVctEvent);
                    break;
                case "emitSosErrorEvent":
                    topicHandlers.put(pattern, this::handleEmitSosErrorEvent);
                    break;
                case "emitRsnEvent":
                    topicHandlers.put(pattern, this::handleEmitRsnEvent);
                    break;
                case "emitRsnVctEvent":
                    topicHandlers.put(pattern, this::handleEmitRsnVctEvent);
                    break;
                case "emitRsnErrorEvent":
                    topicHandlers.put(pattern, this::handleEmitRsnErrorEvent);
                    break;
                case "emitVlsStartCommandResult":
                    topicHandlers.put(pattern, this::handleEmitVlsStartCommandResult);
                    break;
                case "emitVlsVehicleReport":
                    topicHandlers.put(pattern, this::handleEmitVlsVehicleReport);
                    break;
                case "emitVlsStopCommandResult":
                    topicHandlers.put(pattern, this::handleEmitVlsStopCommandResult);
                    break;
                case "emitVoiceCallEvent":
                    topicHandlers.put(pattern, this::handleEmitVoiceCallEvent);
                    break;
                case "emitTrackingVctEvent":
                    topicHandlers.put(pattern, this::handleEmitTrackingVctEvent);
                    break;
                case "publishProvisioningResult":
                    topicHandlers.put(pattern, this::handlePublishProvisioningResult);
                    break;
                case "emitCustActivationRequest":
                    topicHandlers.put(pattern, this::handleEmitCustActivationRequest);
                    break;
                case "publishCustResult":
                    topicHandlers.put(pattern, this::handlePublishCustResult);
                    break;
                case "emitHealthCheckEvent":
                    topicHandlers.put(pattern, this::handleEmitHealthCheckEvent);
                    break;
                case "emitDhcErrorEvent":
                    topicHandlers.put(pattern, this::handleEmitDhcErrorEvent);
                    break;
                case "emitVoiceKillResult":
                    topicHandlers.put(pattern, this::handleEmitVoiceKillResult);
                    break;
                default:
                    log.warn("Unknown topic: {}", topic);
                    break;
            }
        });

        log.info("Initialized MQTT topic handlers for patterns: {}", topicHandlers.keySet());
    }
    // ------------------------------------------------------------------------------------------------ //


    // ACN Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitAcnEvent(Message<?> message) {

        String topic = null;
        List<String> parameters = null;

        // Response Type
        MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.ACK;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            AcnPayload acnPayload = convertPayloadToObject(message.getPayload(), AcnPayload.class);

            // Session 정보 저장
            // ------------------------------------------------------------------------------- //
            String sessionId = acnPayload.getHeader().getUserProperties().getSessionId();
            sessionManage.setSessionId(parameters.get(0), MessageInfo.ServiceEnum.ACN.toString(), sessionId);

            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.ACN.toString(), acnPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId == null) {

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    message.getPayload().toString(),    // Payload
                    ServiceLogTypes.ACN,                // 서비스 Type
                    ServiceLogSystems.DCM,              // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                message.getPayload().toString(),                    // Payload
                teleSvcMainId,                                      // 마스터 로그 식별 ID
                ServiceLogSystems.DCM,                              // 출발지
                ServiceLogSystems.MQTT,                             // 목적지
                true,                                    // 결과
                ServiceLogContents.NOTIFICATION_SEND_DCM_TO_MQTT    // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Payload 송신
            messageType = sendPayload(topic, acnPayload, getRestBaseUrl(), parameters.get(3));

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            boolean resultFlag = messageType == MessageInfo.TypeEnum.ACK ? true : false;

            saveServiceDetailLog(
                message.getPayload().toString(),                    // Payload
                teleSvcMainId,                                      // 마스터 로그 식별 ID
                ServiceLogSystems.MQTT,                             // 출발지
                ServiceLogSystems.CTI,                              // 목적지
                resultFlag,                                         // 결과
                ServiceLogContents.ACN_DATA_SEND_MQTT_TO_CTI        // 로그내용
            );

            if(!resultFlag) {
                mqttService.updateStatusAndTime(ServiceLogStatusCode.FAILURE, LocalDateTime.now(), sessionId);
            }
            // ------------------------------------------------------------------------------- //
        }
        catch (Exception e) {
            messageType = MessageInfo.TypeEnum.NACK;
            log.error("Failed to transmit the message to CCW", e);
        }

        // Response 송신
        handleAcnResponse(message, parameters, messageType);
    }
    // ------------------------------------------------------------------------------------------------ //


    // ACN Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void handleAcnResponse(Message<?> message, List<String> parameters, MessageInfo.TypeEnum messageType) {

        String correlationId = "", sessionId = "";
        String topic = null;

        try {
            if(messageType == MessageInfo.TypeEnum.ACK) {

                // Session ID 및 Correlation ID 유지
                // --------------------------------------------------------------------------- //
                correlationId = sessionManage.getCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.ACN.toString());
                sessionId = sessionManage.getSessionid(parameters.get(0), MessageInfo.ServiceEnum.ACN.toString());
                // --------------------------------------------------------------------------- //
            }
            else {
                // Topic 정보 추출
                topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

                // parameters 정보 추출
                parameters = decodeTopic(topic);

                String payload = (String) message.getPayload();

                JsonNode payloadNode = convertJsonToJsonNode(payload);

                correlationId = payloadNode.at("/header/userProperties/correlationId").asText();
                sessionId = payloadNode.at("/header/userProperties/sessionId").asText();
            }

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {
                sendAcnResponse(parameters, correlationId, sessionId, messageType);
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
            }
        }
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // send ACN Response
    // ------------------------------------------------------------------------------------------------ //
    private void sendAcnResponse(List<String> parameters, String correlationId, String sessionId, MessageInfo.TypeEnum messageType) {

        boolean resultFlag = true;
        AcnResponsePayload acnResponsePayload = new AcnResponsePayload();

        try {
            CommonHeader responseHeader = new CommonHeader();

            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            UserProperties serverUserProperties = new UserProperties();
            serverUserProperties.setCorrelationId(correlationId);
            serverUserProperties.setSessionId(sessionId);
            responseHeader.setUserProperties(serverUserProperties);
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            MessageInfo responseMessage = new MessageInfo();
            responseMessage.setType(messageType);
            responseMessage.setService(MessageInfo.ServiceEnum.ACN);
            responseMessage.setOperation(MessageInfo.OperationEnum.NOTIFICATION);
            responseHeader.setMessage(responseMessage);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            responseHeader.setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            // Header 설정
            acnResponsePayload.setHeader(responseHeader);

            // Publishing Response to Broker
            String topic = publisherService.receiveAcnResponse(acnResponsePayload, parameters.get(0), parameters.get(1), parameters.get(2));

            // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
            //sendPayload(topic, acnResponsePayload, getRestBaseUrl(), "response");
        }
        catch (Exception e) {
            log.error("Failed to send response", e);
            resultFlag = false;
        }

        // Service Transaction Detail Log 기록
        // ----------------------------------------------------------------------------------- //
        try {
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    objectMapper.writeValueAsString(acnResponsePayload),    // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.MQTT,                                 // 출발지
                    ServiceLogSystems.DCM,                                  // 목적지
                    resultFlag,                                             // 결과
                    ServiceLogContents.NOTIFICATION_ACK_MQTT_TO_DCM         // 로그내용
                );

                if(!resultFlag) {
                    mqttService.updateStatusAndTime(ServiceLogStatusCode.FAILURE, LocalDateTime.now(), sessionId);
                }
            }
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send response", e);
        }
        // ----------------------------------------------------------------------------------- //
    }
    // ------------------------------------------------------------------------------------------------ //


    // ACN VCT Message를 CCW로 전달 (CCW에서 응답 처리)
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitAcnVctEvent(Message<?> message) {

        String topic = null;
        List<String> parameters = null;
        MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.ACK;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            AcnVctPayload acnVctPayload = convertPayloadToObject(message.getPayload(), AcnVctPayload.class);

            // Session 정보 저장 (Session ID는 동일)
            // ------------------------------------------------------------------------------- //
            String sessionId = acnVctPayload.getHeader().getUserProperties().getSessionId();

            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.ACN.toString(), acnVctPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                        // Payload
                    mqttService.getTeleSvcMainIdBySessionId(sessionId),     // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                                  // 출발지
                    ServiceLogSystems.MQTT,                                 // 목적지
                    true,                                        // 결과
                    ServiceLogContents.VOICE_CALL_TERMINATOR_DCM_TO_MQTT    // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Payload 송신
            //sendPayload(topic, acnVctPayload, getRestBaseUrl(), parameters.get(3));

            // 통화 종료 관련 정보를 DB에서 확인하여 ACK 또는 NACK 여부 확인
            // ------------------------------------------------------------------------------- //
            CallTermination callTermination = acnVctPayload.getBody().getCallTermination();

            if(callTermination == CallTermination.OTHER) {

                EcallEndHistoryEntity ecallEndHistoryEntity = mqttService.getEcallEnd(sessionId, MessageInfo.ServiceEnum.ACN.toString());

                if(ecallEndHistoryEntity == null) {
                    messageType = MessageInfo.TypeEnum.NACK;
                }
            }
            // ------------------------------------------------------------------------------- //
        }
        catch (Exception e) {
            messageType = MessageInfo.TypeEnum.NACK;
            log.error("Failed to transmit the message to CCW", e);
        }

        handleAcnVctResponse(message, parameters, messageType);
    }
    // ------------------------------------------------------------------------------------------------ //


    // ACN VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void handleAcnVctResponse(Message<?> message, List<String> parameters, MessageInfo.TypeEnum messageType) {

        String correlationId = "", sessionId = "";
        String topic = null;

        try {
            if(messageType == MessageInfo.TypeEnum.ACK) {

                // Session ID 및 Correlation ID 유지
                // --------------------------------------------------------------------------- //
                correlationId = sessionManage.getCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.ACN.toString());
                sessionId = sessionManage.getSessionid(parameters.get(0), MessageInfo.ServiceEnum.ACN.toString());
                // --------------------------------------------------------------------------- //
            }
            else {
                // Topic 정보 추출
                topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

                // parameters 정보 추출
                parameters = decodeTopic(topic);

                String payload = (String) message.getPayload();

                JsonNode payloadNode = convertJsonToJsonNode(payload);

                correlationId = payloadNode.at("/header/userProperties/correlationId").asText();
                sessionId = payloadNode.at("/header/userProperties/sessionId").asText();
            }

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {
                sendAcnVctResponse(parameters, correlationId, sessionId, messageType);
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
            }
        }
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // Send ACN VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void sendAcnVctResponse(List<String> parameters, String correlationId, String sessionId, MessageInfo.TypeEnum messageType) {

        boolean resultFlag = true;
        AcnVctResponsePayload vctResponsePayload = new AcnVctResponsePayload();

        try {
            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            vctResponsePayload.getHeader().getUserProperties().setCorrelationId(correlationId);
            vctResponsePayload.getHeader().getUserProperties().setSessionId(sessionId);
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            vctResponsePayload.getHeader().getMessage().setType(messageType);
            vctResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.ACN);
            vctResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.CONFIRM_TERMINATION);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            vctResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            // Session 삭제 여부 확인할 것!
            // sessionManage.removeSessionInfo(responseParam.getDeviceId());

            // Publishing Response to Broker
            String topic = publisherService.receiveAcnVctResponse(vctResponsePayload, parameters.get(0), parameters.get(1), parameters.get(2));

            // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
            //sendPayload(topic, vctResponsePayload, getRestBaseUrl(), "response");
        }
        catch (Exception e) {
            log.error("Failed to send response", e);
            resultFlag = false;
        }

        // Service Transaction Detail Log 기록
        // ----------------------------------------------------------------------------------- //
        try {
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    objectMapper.writeValueAsString(vctResponsePayload),            // Payload
                    mqttService.getTeleSvcMainIdBySessionId(sessionId),             // 마스터 로그 식별 ID
                    ServiceLogSystems.MQTT,                                         // 출발지
                    ServiceLogSystems.DCM,                                          // 목적지
                    resultFlag,                                                     // 결과
                    ServiceLogContents.VOICE_CALL_TERMINATOR_RESPONSE_MQTT_TO_DCM   // 로그내용
                );

                if(!resultFlag) {
                    mqttService.updateStatusAndTime(ServiceLogStatusCode.FAILURE, LocalDateTime.now(), sessionId);
                }
            }
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send response", e);
        }
        // ----------------------------------------------------------------------------------- //

        // VCT ACK인 경우, DCM 오작동으로 인한 Call 재개 방지를 위해 VoiceKill 송신
        // ----------------------------------------------------------------------------------- //
        if(messageType == MessageInfo.TypeEnum.ACK) {
            handleVoiceKillRequest(parameters, MessageInfo.ServiceEnum.ACN);
        }
        // ----------------------------------------------------------------------------------- //
    }
    // ------------------------------------------------------------------------------------------------ //


    // ACN Error Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitAcnErrorEvent(Message<?> message) {
        handleEmitErrorEvent(message, MessageInfo.ServiceEnum.ACN);
    }
    // ------------------------------------------------------------------------------------------------ //


    // SOS Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitSosEvent(Message<?> message) {

        String topic = null;
        List<String> parameters = null;

        // Response Type
        MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.ACK;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            SosPayload sosPayload = convertPayloadToObject(message.getPayload(), SosPayload.class);

            // Session 정보 저장
            // ------------------------------------------------------------------------------- //
            String sessionId = sosPayload.getHeader().getUserProperties().getSessionId();
            sessionManage.setSessionId(parameters.get(0), MessageInfo.ServiceEnum.SOS.toString(), sessionId);

            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.SOS.toString(), sosPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId == null) {

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    message.getPayload().toString(),    // Payload
                    ServiceLogTypes.SOS,                // 서비스 Type
                    ServiceLogSystems.DCM,              // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                message.getPayload().toString(),                    // Payload
                teleSvcMainId,                                      // 마스터 로그 식별 ID
                ServiceLogSystems.DCM,                              // 출발지
                ServiceLogSystems.MQTT,                             // 목적지
                true,                                    // 결과
                ServiceLogContents.NOTIFICATION_SEND_DCM_TO_MQTT    // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Topic 및 Payload 전달
            messageType = sendPayload(topic, sosPayload, getRestBaseUrl(), parameters.get(3));

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            boolean resultFlag = messageType == MessageInfo.TypeEnum.ACK ? true : false;

            saveServiceDetailLog(
                message.getPayload().toString(),                    // Payload
                teleSvcMainId,                                      // 마스터 로그 식별 ID
                ServiceLogSystems.MQTT,                             // 출발지
                ServiceLogSystems.CTI,                              // 목적지
                resultFlag,                                         // 결과
                ServiceLogContents.SOS_DATA_SEND_MQTT_TO_CTI        // 로그내용
            );

            if(!resultFlag) {
                mqttService.updateStatusAndTime(ServiceLogStatusCode.FAILURE, LocalDateTime.now(), sessionId);
            }
            // ------------------------------------------------------------------------------- //
        }
        catch (Exception e) {
            messageType = MessageInfo.TypeEnum.NACK;
            log.error("Failed to transmit the message to CCW", e);
        }

        // Response 송신
        handleSosResponse(message, parameters, messageType);
    }
    // ------------------------------------------------------------------------------------------------ //


    // SOS Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void handleSosResponse(Message<?> message, List<String> parameters, MessageInfo.TypeEnum messageType) {

        String correlationId = "", sessionId = "";
        String topic = null;

        try {
            if(messageType == MessageInfo.TypeEnum.ACK) {

                // Session ID 및 Correlation ID 유지
                // --------------------------------------------------------------------------- //
                correlationId = sessionManage.getCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.SOS.toString());
                sessionId = sessionManage.getSessionid(parameters.get(0), MessageInfo.ServiceEnum.SOS.toString());
                // --------------------------------------------------------------------------- //
            }
            else {
                // Topic 정보 추출
                topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

                // parameters 정보 추출
                parameters = decodeTopic(topic);

                String payload = (String) message.getPayload();

                JsonNode payloadNode = convertJsonToJsonNode(payload);

                correlationId = payloadNode.at("/header/userProperties/correlationId").asText();
                sessionId = payloadNode.at("/header/userProperties/sessionId").asText();
            }

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {
                sendSosResponse(parameters, correlationId, sessionId, messageType);
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
            }
        }
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // Send SOS Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void sendSosResponse(List<String> parameters, String correlationId, String sessionId, MessageInfo.TypeEnum messageType) {

        boolean resultFlag = true;
        SosResponsePayload sosResponsePayload = new SosResponsePayload();

        try {
            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            sosResponsePayload.getHeader().getUserProperties().setCorrelationId(correlationId);
            sosResponsePayload.getHeader().getUserProperties().setSessionId(sessionId);
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            sosResponsePayload.getHeader().getMessage().setType(messageType);
            sosResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.SOS);
            sosResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.NOTIFICATION);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            sosResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            // Publishing Response to Broker
            String topic = publisherService.receiveSosResponse(sosResponsePayload, parameters.get(0), parameters.get(1), parameters.get(2));

            // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
            //sendPayload(topic, sosResponsePayload, getRestBaseUrl(), "response");
        }
        catch (Exception e) {
            log.error("Failed to send response", e);
            resultFlag = false;
        }

        // Service Transaction Detail Log 기록
        // ----------------------------------------------------------------------------------- //
        try {
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    objectMapper.writeValueAsString(sosResponsePayload),    // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.MQTT,                                 // 출발지
                    ServiceLogSystems.DCM,                                  // 목적지
                    resultFlag,                                             // 결과
                    ServiceLogContents.NOTIFICATION_ACK_MQTT_TO_DCM         // 로그내용
                );

                if(!resultFlag) {
                    mqttService.updateStatusAndTime(ServiceLogStatusCode.FAILURE, LocalDateTime.now(), sessionId);
                }
            }
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send response", e);
        }
        // ----------------------------------------------------------------------------------- //
    }
    // ------------------------------------------------------------------------------------------------ //


    // SOS VCT Message를 CCW로 전달 (CCW에서 응답 처리)
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitSosVctEvent(Message<?> message) {

        String topic = null;
        List<String> parameters = null;
        MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.ACK;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            SosVctPayload sosVctPayload = convertPayloadToObject(message.getPayload(), SosVctPayload.class);

            // Session 정보 저장 (Session ID는 동일)
            // ------------------------------------------------------------------------------- //
            String sessionId = sosVctPayload.getHeader().getUserProperties().getSessionId();

            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.SOS.toString(), sosVctPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                        // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                                  // 출발지
                    ServiceLogSystems.MQTT,                                 // 목적지
                    true,                                        // 결과
                    ServiceLogContents.VOICE_CALL_TERMINATOR_DCM_TO_MQTT    // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Topic 및 Payload 전달
            //sendPayload(topic, sosVctPayload, getRestBaseUrl(), parameters.get(3));

            // 통화 종료 관련 정보를 DB에서 확인하여 ACK 또는 NACK 여부 확인
            // ------------------------------------------------------------------------------- //
            CallTermination callTermination = sosVctPayload.getBody().getCallTermination();

            if(callTermination == CallTermination.OTHER) {

                EcallEndHistoryEntity ecallEndHistoryEntity = mqttService.getEcallEnd(sessionId, MessageInfo.ServiceEnum.SOS.toString());

                if(ecallEndHistoryEntity == null) {
                    messageType = MessageInfo.TypeEnum.NACK;
                }
            }
            // ------------------------------------------------------------------------------- //
        }
        catch (Exception e) {
            messageType = MessageInfo.TypeEnum.NACK;
            log.error("Failed to transmit the message to CCW", e);
        }

        handleSosVctResponse(message, parameters, messageType);
    }
    // ------------------------------------------------------------------------------------------------ //


    // SOS VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void handleSosVctResponse(Message<?> message, List<String> parameters, MessageInfo.TypeEnum messageType) {

        String correlationId = "", sessionId = "";
        String topic = null;

        try {
            if(messageType == MessageInfo.TypeEnum.ACK) {

                // Session ID 및 Correlation ID 유지
                // --------------------------------------------------------------------------- //
                correlationId = sessionManage.getCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.SOS.toString());
                sessionId = sessionManage.getSessionid(parameters.get(0), MessageInfo.ServiceEnum.SOS.toString());
                // --------------------------------------------------------------------------- //
            }
            else {
                // Topic 정보 추출
                topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

                // parameters 정보 추출
                parameters = decodeTopic(topic);

                String payload = (String) message.getPayload();

                JsonNode payloadNode = convertJsonToJsonNode(payload);

                correlationId = payloadNode.at("/header/userProperties/correlationId").asText();
                sessionId = payloadNode.at("/header/userProperties/sessionId").asText();
            }

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {
                sendSosVctResponse(parameters, correlationId, sessionId, messageType);
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
            }
        }
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // Send SOS VCT Message를 CCW로 전달 (CCW에서 응답 처리)
    // ------------------------------------------------------------------------------------------------ //
    private void sendSosVctResponse(List<String> parameters, String correlationId, String sessionId, MessageInfo.TypeEnum messageType) {

        boolean resultFlag = true;
        SosVctResponsePayload vctResponsePayload = new SosVctResponsePayload();

        try {
            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            vctResponsePayload.getHeader().getUserProperties().setCorrelationId(correlationId);
            vctResponsePayload.getHeader().getUserProperties().setSessionId(sessionId);
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            vctResponsePayload.getHeader().getMessage().setType(messageType);
            vctResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.SOS);
            vctResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.CONFIRM_TERMINATION);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            vctResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            // Session 삭제 여부 확인할 것!
            // sessionManage.removeSessionInfo(responseParam.getDeviceId());

            // Publishing Response to Broker
            String topic = publisherService.receiveSosVctResponse(vctResponsePayload, parameters.get(0), parameters.get(1), parameters.get(2));

            // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
            //sendPayload(topic, vctResponsePayload, getRestBaseUrl(), "response");
        }
        catch (Exception e) {
            log.error("Failed to send response", e);
            resultFlag = false;
        }

        // Service Transaction Detail Log 기록
        // ----------------------------------------------------------------------------------- //
        try {
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    objectMapper.writeValueAsString(vctResponsePayload),            // Payload
                    mqttService.getTeleSvcMainIdBySessionId(sessionId),             // 마스터 로그 식별 ID
                    ServiceLogSystems.MQTT,                                         // 출발지
                    ServiceLogSystems.DCM,                                          // 목적지
                    resultFlag,                                                     // 결과
                    ServiceLogContents.VOICE_CALL_TERMINATOR_RESPONSE_MQTT_TO_DCM   // 로그내용
                );
            }
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send response", e);
        }
        // ----------------------------------------------------------------------------------- //

        // VCT ACK인 경우, DCM 오작동으로 인한 Call 재개 방지를 위해 VoiceKill 송신
        // ----------------------------------------------------------------------------------- //
        if(messageType == MessageInfo.TypeEnum.ACK) {
            handleVoiceKillRequest(parameters, MessageInfo.ServiceEnum.SOS);
        }
        // ----------------------------------------------------------------------------------- //
    }
    // ------------------------------------------------------------------------------------------------ //


    // SOS Error Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitSosErrorEvent(Message<?> message) {
        handleEmitErrorEvent(message, MessageInfo.ServiceEnum.SOS);
    }
    // ------------------------------------------------------------------------------------------------ //


    // RSN Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitRsnEvent(Message<?> message) {

        String topic = null;
        List<String> parameters = null;

        // Response Type
        MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.ACK;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            RsnPayload rsnPayload = convertPayloadToObject(message.getPayload(), RsnPayload.class);

            // Session 정보 저장
            // ------------------------------------------------------------------------------- //
            String sessionId = rsnPayload.getHeader().getUserProperties().getSessionId();
            sessionManage.setSessionId(parameters.get(0), MessageInfo.ServiceEnum.RSN.toString(), sessionId);

            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.RSN.toString(), rsnPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId == null) {

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    message.getPayload().toString(),    // Payload
                    ServiceLogTypes.RSN,                // 서비스 Type
                    ServiceLogSystems.DCM,              // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                message.getPayload().toString(),                    // Payload
                teleSvcMainId,                                      // 마스터 로그 식별 ID
                ServiceLogSystems.DCM,                              // 출발지
                ServiceLogSystems.MQTT,                             // 목적지
                true,                                    // 결과
                ServiceLogContents.NOTIFICATION_SEND_DCM_TO_MQTT    // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Topic 및 Payload 전달
            messageType = sendPayload(topic, rsnPayload, getRestBaseUrl(), parameters.get(3));

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            boolean resultFlag = messageType == MessageInfo.TypeEnum.ACK ? true : false;

            saveServiceDetailLog(
                message.getPayload().toString(),                    // Payload
                teleSvcMainId,                                      // 마스터 로그 식별 ID
                ServiceLogSystems.MQTT,                             // 출발지
                ServiceLogSystems.CTI,                              // 목적지
                resultFlag,                                         // 결과
                ServiceLogContents.RSN_DATA_SEND_MQTT_TO_CTI        // 로그내용
            );

            if(!resultFlag) {
                mqttService.updateStatusAndTime(ServiceLogStatusCode.FAILURE, LocalDateTime.now(), sessionId);
            }
            // ------------------------------------------------------------------------------- //
        }
        catch (Exception e) {
            messageType = MessageInfo.TypeEnum.NACK;
            log.error("Failed to transmit the message to CCW", e);
        }

        // Response 송신
        handleRsnResponse(message, parameters, messageType);
    }
    // ------------------------------------------------------------------------------------------------ //


    // RSN Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void handleRsnResponse(Message<?> message, List<String> parameters, MessageInfo.TypeEnum messageType) {

        String correlationId = "", sessionId = "";
        String topic = null;

        try {
            if(messageType == MessageInfo.TypeEnum.ACK) {

                // Session ID 및 Correlation ID 유지
                // ----------------------------------------------------------------------------------- //
                correlationId = sessionManage.getCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.RSN.toString());
                sessionId = sessionManage.getSessionid(parameters.get(0), MessageInfo.ServiceEnum.RSN.toString());
                // ----------------------------------------------------------------------------------- //
            }
            else {
                // Topic 정보 추출
                topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

                // parameters 정보 추출
                parameters = decodeTopic(topic);

                String payload = (String) message.getPayload();

                JsonNode payloadNode = convertJsonToJsonNode(payload);

                correlationId = payloadNode.at("/header/userProperties/correlationId").asText();
                sessionId = payloadNode.at("/header/userProperties/sessionId").asText();
            }

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {
                sendRsnResponse(parameters, correlationId, sessionId, messageType);
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
            }
        }
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // Send RSN Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void sendRsnResponse(List<String> parameters, String correlationId, String sessionId, MessageInfo.TypeEnum messageType) {

        boolean resultFlag = true;
        RsnResponsePayload rsnResponsePayload = new RsnResponsePayload();

        try {
            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            rsnResponsePayload.getHeader().getUserProperties().setCorrelationId(correlationId);
            rsnResponsePayload.getHeader().getUserProperties().setSessionId(sessionId);
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            rsnResponsePayload.getHeader().getMessage().setType(messageType);
            rsnResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.RSN);
            rsnResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.NOTIFICATION);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            rsnResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            // Publishing Response to Broker
            String topic = publisherService.receiveRsnResponse(rsnResponsePayload, parameters.get(0), parameters.get(1), parameters.get(2));

            // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
            //sendPayload(topic, rsnResponsePayload, getRestBaseUrl(), "response");
        }
        catch (Exception e) {
            log.error("Failed to send response", e);
            resultFlag = false;
        }

        // Service Transaction Detail Log 기록
        // ----------------------------------------------------------------------------------- //
        try {
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    objectMapper.writeValueAsString(rsnResponsePayload),    // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.MQTT,                                 // 출발지
                    ServiceLogSystems.DCM,                                  // 목적지
                    resultFlag,                                             // 결과
                    ServiceLogContents.NOTIFICATION_ACK_MQTT_TO_DCM         // 로그내용
                );

                if(!resultFlag) {
                    mqttService.updateStatusAndTime(ServiceLogStatusCode.FAILURE, LocalDateTime.now(), sessionId);
                }
            }
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send response", e);
        }
        // ----------------------------------------------------------------------------------- //
    }
    // ------------------------------------------------------------------------------------------------ //


    // RSN VCT Message를 CCW로 전달 (CCW에서 응답 처리)
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitRsnVctEvent(Message<?> message) {

        String topic = null;
        List<String> parameters = null;
        MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.ACK;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            RsnVctPayload rsnVctPayload = convertPayloadToObject(message.getPayload(), RsnVctPayload.class);

            // Session 정보 저장 (Session ID는 동일)
            // ------------------------------------------------------------------------------- //
            String sessionId = rsnVctPayload.getHeader().getUserProperties().getSessionId();

            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.RSN.toString(), rsnVctPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                        // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                                  // 출발지
                    ServiceLogSystems.MQTT,                                 // 목적지
                    true,                                        // 결과
                    ServiceLogContents.VOICE_CALL_TERMINATOR_DCM_TO_MQTT    // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Topic 및 Payload 전달
            //sendPayload(topic, rsnVctPayload, getRestBaseUrl(), parameters.get(3));

            // 통화 종료 관련 정보를 DB에서 확인하여 ACK 또는 NACK 여부 확인
            // ------------------------------------------------------------------------------- //
            CallTermination callTermination = rsnVctPayload.getBody().getCallTermination();

            if(callTermination == CallTermination.OTHER) {

                EcallEndHistoryEntity ecallEndHistoryEntity = mqttService.getEcallEnd(sessionId, MessageInfo.ServiceEnum.RSN.toString());

                if(ecallEndHistoryEntity == null) {
                    messageType = MessageInfo.TypeEnum.NACK;
                }
            }
            // ------------------------------------------------------------------------------- //
        }
        catch (Exception e) {
            messageType = MessageInfo.TypeEnum.NACK;
            log.error("Failed to transmit the message to CCW", e);
        }

        handleRsnVctResponse(message, parameters, messageType);
    }
    // ------------------------------------------------------------------------------------------------ //


    // RSN VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void handleRsnVctResponse(Message<?> message, List<String> parameters, MessageInfo.TypeEnum messageType) {

        String correlationId = "", sessionId = "";
        String topic = null;

        try {
            if(messageType == MessageInfo.TypeEnum.ACK) {

                // Session ID 및 Correlation ID 유지
                // ----------------------------------------------------------------------------------- //
                correlationId = sessionManage.getCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.RSN.toString());
                sessionId = sessionManage.getSessionid(parameters.get(0), MessageInfo.ServiceEnum.RSN.toString());
                // ----------------------------------------------------------------------------------- //
            }
            else {
                // Topic 정보 추출
                topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

                // parameters 정보 추출
                parameters = decodeTopic(topic);

                String payload = (String) message.getPayload();

                JsonNode payloadNode = convertJsonToJsonNode(payload);

                correlationId = payloadNode.at("/header/userProperties/correlationId").asText();
                sessionId = payloadNode.at("/header/userProperties/sessionId").asText();
            }

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {
                sendRsnVctResponse(parameters, correlationId, sessionId, messageType);
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
            }
        }
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // Send RSN VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void sendRsnVctResponse(List<String> parameters, String correlationId, String sessionId, MessageInfo.TypeEnum messageType) {

        boolean resultFlag = true;
        RsnVctResponsePayload vctResponsePayload = new RsnVctResponsePayload();

        try {
            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            vctResponsePayload.getHeader().getUserProperties().setCorrelationId(correlationId);
            vctResponsePayload.getHeader().getUserProperties().setSessionId(sessionId);
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            vctResponsePayload.getHeader().getMessage().setType(messageType);
            vctResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.RSN);
            vctResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.CONFIRM_TERMINATION);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            vctResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            // Session 삭제 여부 확인할 것!
            // sessionManage.removeSessionInfo(responseParam.getDeviceId());

            // Publishing Response to Broker
            String topic = publisherService.receiveRsnVctResponse(vctResponsePayload, parameters.get(0), parameters.get(1), parameters.get(2));

            // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
            //sendPayload(topic, vctResponsePayload, getRestBaseUrl(), "response");
        }
        catch (Exception e) {
            log.error("Failed to send the response to DCM", e);
            resultFlag = false;
        }

        // Service Transaction Detail Log 기록
        // ----------------------------------------------------------------------------------- //
        try {
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    objectMapper.writeValueAsString(vctResponsePayload),            // Payload
                    teleSvcMainId,                                                  // 마스터 로그 식별 ID
                    ServiceLogSystems.MQTT,                                         // 출발지
                    ServiceLogSystems.DCM,                                          // 목적지
                    resultFlag,                                                     // 결과
                    ServiceLogContents.VOICE_CALL_TERMINATOR_RESPONSE_MQTT_TO_DCM   // 로그내용
                );

                if(!resultFlag) {
                    mqttService.updateStatusAndTime(ServiceLogStatusCode.FAILURE, LocalDateTime.now(), sessionId);
                }
            }
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send response", e);
        }
        // ----------------------------------------------------------------------------------- //

        // VCT ACK인 경우, DCM 오작동으로 인한 Call 재개 방지를 위해 VoiceKill 송신
        // ----------------------------------------------------------------------------------- //
        if(messageType == MessageInfo.TypeEnum.ACK) {
            handleVoiceKillRequest(parameters, MessageInfo.ServiceEnum.RSN);
        }
        // ----------------------------------------------------------------------------------- //
    }
    // ------------------------------------------------------------------------------------------------ //


    // RSN Error Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitRsnErrorEvent(Message<?> message) {
        handleEmitErrorEvent(message, MessageInfo.ServiceEnum.RSN);
    }
    // ------------------------------------------------------------------------------------------------ //


    // VLS Start Result Message를 CCW로 전달 (CCW requests DCM to start tracking first.)
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitVlsStartCommandResult(Message<?> message) {

        String topic = null;
        List<String> parameters = null;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            VlsStartResultPayload VlsStartResultPayload = convertPayloadToObject(message.getPayload(), VlsStartResultPayload.class);

            // Session 정보 저장 (Session ID는 동일)
            // ------------------------------------------------------------------------------- //
            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.VLS.toString(), VlsStartResultPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            String sessionId = VlsStartResultPayload.getHeader().getUserProperties().getSessionId();

            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                        // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                                  // 출발지
                    ServiceLogSystems.MQTT,                                 // 목적지
                    true,                                        // 결과
                    ServiceLogContents.NOTIFICATION_SEND_DCM_TO_MQTT        // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Payload 송신
            //sendPayload(topic, VlsStartResultPayload, getRestBaseUrl(), parameters.get(3));
        }
        catch (Exception e) {
            log.error("Failed to transmit the message to CCW", e);
        }

        /* Session Management
           DCM에서 시작하는 경우 DeviceUserProperties 삭제
        */
        //sessionManage.removeSessionInfo(parameters.get(0).toString());

        // emitVlsStartCommandResult(parameters.get(0), parameters.get(1), parameters.get(2), VlsStartResultPayload);
    }
    // ------------------------------------------------------------------------------------------------ //


    // Vehicle Tracking Report를 CCW로 전달
    /* DCM will send vehicle reports at the configured interval until a stop is requested by CCW */
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitVlsVehicleReport(Message<?> message) {

        String topic = null;
        List<String> parameters = null;

        // Response Type
        MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.ACK;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            VlsPayload vlsPayload = convertPayloadToObject(message.getPayload(), VlsPayload.class);

            // Session 정보 저장
            // ------------------------------------------------------------------------------- //
            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.VLS.toString(), vlsPayload.getHeader().getUserProperties().getCorrelationId());
            // IG_OFF 동작 후 IG_ON 될 경우 SessionId 변경되어 SessionId 저장함.
            sessionManage.setSessionId(parameters.get(0), MessageInfo.ServiceEnum.VLS.toString(), vlsPayload.getHeader().getUserProperties().getSessionId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            String sessionId = vlsPayload.getHeader().getUserProperties().getSessionId();

            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                        // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                                  // 출발지
                    ServiceLogSystems.MQTT,                                 // 목적지
                    true,                                        // 결과
                    ServiceLogContents.VLS_REPORT_REQUEST_DCM_TO_MQTT       // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Payload 송신
            //messageType = sendPayload(topic, vlsPayload, getRestBaseUrl(), parameters.get(3));
        }
        catch (Exception e) {
            messageType = MessageInfo.TypeEnum.NACK;
            log.error("Failed to transmit the message to CCW", e);
        }

        // Response 송신
        handleVlsVehicleReportResponse(message, parameters, messageType);
    }
    // ------------------------------------------------------------------------------------------------ //


    // Vehicle Tracking Report에 대한 Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void handleVlsVehicleReportResponse(Message<?> message, List<String> parameters, MessageInfo.TypeEnum messageType) {

        String correlationId = "", sessionId = "";
        String topic = null;

        try {
            if(messageType == MessageInfo.TypeEnum.ACK) {
                /* SessionId 저장으로 변경하여 주석 처리.
                VlsPayload vlsPayload = convertPayloadToObject(message.getPayload(), VlsPayload.class);

                VlsEventTrigger eventTrigger = vlsPayload.getBody().getVehicleReport().getEventTrigger();

                if (eventTrigger != null && eventTrigger.equals(VlsEventTrigger.BATTERY_DISCONNECT)) {
                    //Session Initiated By DCM : New vlsPayload packet generated with eventTrigger = BATTERY_DISCONNECT
                    correlationId = vlsPayload.getHeader().getUserProperties().getCorrelationId();
                    sessionId = vlsPayload.getHeader().getUserProperties().getSessionId();
                }
                else
                */
                {
                    // Session ID 및 Correlation ID 유지
                    // ----------------------------------------------------------------------------------- //
                    correlationId = sessionManage.getCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.VLS.toString());
                    sessionId = sessionManage.getSessionid(parameters.get(0), MessageInfo.ServiceEnum.VLS.toString());
                    // ----------------------------------------------------------------------------------- //
                }
            }
            else {
                // Topic 정보 추출
                topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

                // parameters 정보 추출
                parameters = decodeTopic(topic);

                String payload = (String) message.getPayload();

                JsonNode payloadNode = convertJsonToJsonNode(payload);

                correlationId = payloadNode.at("/header/userProperties/correlationId").asText();
                sessionId = payloadNode.at("/header/userProperties/sessionId").asText();
            }

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {
                sendVlsVehicleReportResponse(parameters, correlationId, sessionId, messageType);
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
            }
        }
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // Vehicle Tracking Report에 대한 Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void sendVlsVehicleReportResponse(List<String> parameters, String correlationId, String sessionId, MessageInfo.TypeEnum messageType) {

        boolean resultFlag = true;
        VlsResponsePayload vlsResponsePayload = new VlsResponsePayload();

        try {
            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            vlsResponsePayload.getHeader().getUserProperties().setCorrelationId(correlationId);
            vlsResponsePayload.getHeader().getUserProperties().setSessionId(sessionId);
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            vlsResponsePayload.getHeader().getMessage().setType(messageType);
            vlsResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.VLS);
            vlsResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.REPORT);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            vlsResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            // Publishing Response to Broker
            String topic = publisherService.receiveVlsVehicleReportResponse(vlsResponsePayload, parameters.get(0), parameters.get(1), parameters.get(2));

            // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
            //sendPayload(topic, vlsResponsePayload, getRestBaseUrl(), "response");
        }
        catch (Exception e) {
            log.error("Failed to send the response to DCM", e);
            resultFlag = false;
        }

        // Service Transaction Detail Log 기록
        // ----------------------------------------------------------------------------------- //
        try {
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    objectMapper.writeValueAsString(vlsResponsePayload),    // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.MQTT,                                 // 출발지
                    ServiceLogSystems.DCM,                                  // 목적지
                    resultFlag,                                             // 결과
                    ServiceLogContents.VLS_REPORT_RESPONSE_MQTT_TO_DCM      // 로그내용
                );

                if(!resultFlag) {
                    mqttService.updateStatusAndTime(ServiceLogStatusCode.FAILURE, LocalDateTime.now(), sessionId);
                }
            }
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send response", e);
        }
        // ----------------------------------------------------------------------------------- //
    }
    // ------------------------------------------------------------------------------------------------ //


    // VLS Stop Result Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitVlsStopCommandResult(Message<?> message) {

        String topic = null;
        List<String> parameters = null;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            VlsStopResultPayload vlsStopCommandResultPayload = convertPayloadToObject(message.getPayload(), VlsStopResultPayload.class);

            // Session 정보 저장 (Session ID는 동일)
            // ------------------------------------------------------------------------------- //
            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.VLS.toString(), vlsStopCommandResultPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            String sessionId = vlsStopCommandResultPayload.getHeader().getUserProperties().getSessionId();

            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                        // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                                  // 출발지
                    ServiceLogSystems.MQTT,                                 // 목적지
                    true,                                        // 결과
                    ServiceLogContents.NOTIFICATION_SEND_DCM_TO_MQTT        // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Payload 송신
            //sendPayload(topic, vlsStopCommandResultPayload, getRestBaseUrl(), parameters.get(3));
        }
        catch (Exception e) {
            log.error("Failed to transmit the message to CCW", e);
        }

        /* Session Management
           DCM에서 시작하는 경우 DeviceUserProperties 삭제
        */
        //sessionManage.removeSessionInfo(parameters.get(0).toString());

        // emitVlsStopCommandResult(parameters.get(0), parameters.get(1), parameters.get(2), vlsStopCommandResultPayload);
    }
    // ------------------------------------------------------------------------------------------------ //


    // VLS Voice Call Result Message를 CCW로 전달
    /* Start voice call in parallel to publishing a voice all ACK
       Initiate VLS voice call from CCW/Call center first.
    */
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitVoiceCallEvent(Message<?> message) {

        String topic = null;
        List<String> parameters = null;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            VlsVoiceCallResultPayload vlsVoiceCallResultPayload = convertPayloadToObject(message.getPayload(), VlsVoiceCallResultPayload.class);

            // Session 정보 저장 (Session ID는 동일)
            // ------------------------------------------------------------------------------- //
            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.VLS.toString(), vlsVoiceCallResultPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            String sessionId = vlsVoiceCallResultPayload.getHeader().getUserProperties().getSessionId();

            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                        // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                                  // 출발지
                    ServiceLogSystems.MQTT,                                 // 목적지
                    true,                                        // 결과
                    ServiceLogContents.NOTIFICATION_SEND_DCM_TO_MQTT        // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Payload 송신
            //sendPayload(topic, vlsVoiceCallResultPayload, getRestBaseUrl(), parameters.get(3));
        }
        catch (Exception e) {
            log.error("Failed to transmit the message to CCW", e);
        }

        /* Session Management
           DCM에서 시작하는 경우 DeviceUserProperties 삭제
        */
        //sessionManage.removeSessionInfo(parameters.get(0).toString());
        // emitVoiceCallEvent(parameters.get(0), parameters.get(1), parameters.get(2), vlsVoiceCallResultPayload);
    }
    // ------------------------------------------------------------------------------------------------ //


    // VLS VCT Message를 CCW로 전달 (CCW에서 응답 처리)
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitTrackingVctEvent(Message<?> message) {

        String topic = null;
        List<String> parameters = null;
        MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.ACK;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            VlsVctPayload vlsVctPayload = convertPayloadToObject(message.getPayload(), VlsVctPayload.class);

            // Session 정보 저장 (Session ID는 동일)
            // ------------------------------------------------------------------------------- //
            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.VLS.toString(), vlsVctPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            String sessionId = vlsVctPayload.getHeader().getUserProperties().getSessionId();

            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                        // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                                  // 출발지
                    ServiceLogSystems.MQTT,                                 // 목적지
                    true,                                        // 결과
                    ServiceLogContents.VOICE_CALL_TERMINATOR_DCM_TO_MQTT    // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Topic 및 Payload 전달
            //sendPayload(topic, vlsVctPayload, getRestBaseUrl(), parameters.get(3));

            // 통화 종료 관련 정보를 DB에서 확인하여 ACK 또는 NACK 여부 확인
            // ------------------------------------------------------------------------------- //
            EcallEndHistoryEntity ecallEndHistoryEntity = mqttService.getEcallEnd(sessionId, MessageInfo.ServiceEnum.VLS.toString());

            if(ecallEndHistoryEntity == null) {
                messageType = MessageInfo.TypeEnum.NACK;
            }
            // ------------------------------------------------------------------------------- //
        }
        catch (Exception e) {
            messageType = MessageInfo.TypeEnum.NACK;
            log.error("Failed to transmit the message to CCW", e);
        }

        handleVlsVctResponse(message, parameters, messageType);
    }
    // ------------------------------------------------------------------------------------------------ //


    // VLS VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void handleVlsVctResponse(Message<?> message, List<String> parameters, MessageInfo.TypeEnum messageType) {

        String correlationId = "", sessionId = "";
        String topic = null;

        try {
            if(messageType == MessageInfo.TypeEnum.ACK) {

                // Session ID 및 Correlation ID 유지
                // ----------------------------------------------------------------------------------- //
                correlationId = sessionManage.getCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.VLS.toString());
                sessionId = sessionManage.getSessionid(parameters.get(0), MessageInfo.ServiceEnum.VLS.toString());
                // ----------------------------------------------------------------------------------- //
            }
            else {
                // Topic 정보 추출
                topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

                // parameters 정보 추출
                parameters = decodeTopic(topic);

                String payload = (String) message.getPayload();

                JsonNode payloadNode = convertJsonToJsonNode(payload);

                correlationId = payloadNode.at("/header/userProperties/correlationId").asText();
                sessionId = payloadNode.at("/header/userProperties/sessionId").asText();
            }

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {
                sendVlsVctResponse(parameters, correlationId, sessionId, messageType);
            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
            }
        }
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // Send VLS VCT Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void sendVlsVctResponse(List<String> parameters, String correlationId, String sessionId, MessageInfo.TypeEnum messageType) {

        boolean resultFlag = true;
        VlsVctResponsePayload vctResponsePayload = new VlsVctResponsePayload();

        try {
            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            vctResponsePayload.getHeader().getUserProperties().setCorrelationId(correlationId);
            vctResponsePayload.getHeader().getUserProperties().setSessionId(sessionId);
            // ------------------------------------------------------------------------------- //

            // message 설정
            // ------------------------------------------------------------------------------- //
            vctResponsePayload.getHeader().getMessage().setType(messageType);
            vctResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.VLS);
            vctResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.CONFIRM_TERMINATION);
            // ------------------------------------------------------------------------------- //

            // transmissionTimestampUTC 설정
            vctResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

            // Session 삭제 여부 확인할 것!
            // sessionManage.removeSessionInfo(responseParam.getDeviceId());

            // Publishing Response to Broker
            String topic = publisherService.receiveTrackingVctResponse(vctResponsePayload, parameters.get(0), parameters.get(1), parameters.get(2));

            // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
            //sendPayload(topic, vctResponsePayload, getRestBaseUrl(), "response");
        }
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
            resultFlag = false;
        }

        // Service Transaction Detail Log 기록
        // ----------------------------------------------------------------------------------- //
        try {
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    objectMapper.writeValueAsString(vctResponsePayload),            // Payload
                    teleSvcMainId,                                                  // 마스터 로그 식별 ID
                    ServiceLogSystems.MQTT,                                         // 출발지
                    ServiceLogSystems.DCM,                                          // 목적지
                    resultFlag,                                                     // 결과
                    ServiceLogContents.VOICE_CALL_TERMINATOR_RESPONSE_MQTT_TO_DCM   // 로그내용
                );

                if(!resultFlag) {
                    mqttService.updateStatusAndTime(ServiceLogStatusCode.FAILURE, LocalDateTime.now(), sessionId);
                }
            }
        }
        catch (JsonProcessingException e) {
            log.error("Failed to send response", e);
        }
        // ----------------------------------------------------------------------------------- //

        // VCT ACK인 경우, DCM 오작동으로 인한 Call 재개 방지를 위해 VoiceKill 송신
        // ----------------------------------------------------------------------------------- //
        if(messageType == MessageInfo.TypeEnum.ACK) {
            handleVoiceKillRequest(parameters, MessageInfo.ServiceEnum.VLS);
        }
        // ----------------------------------------------------------------------------------- //
    }
    // ------------------------------------------------------------------------------------------------ //


    // Provisioning Result Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handlePublishProvisioningResult(Message<?> message) {

        String topic = null;
        List<String> parameters = null;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            ProvisioningResultPayload provisioningResultPayload = convertPayloadToObject(message.getPayload(), ProvisioningResultPayload.class);

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            String sessionId = provisioningResultPayload.getHeader().getUserProperties().getSessionId();

            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                            // Payload
                    teleSvcMainId,                                              // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                                      // 출발지
                    ServiceLogSystems.MQTT,                                     // 목적지
                    true,                                            // 결과
                    ServiceLogContents.PROVISIONING_RESULT_SEND_DCM_TO_MQTT     // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Payload 송신
            //sendPayload(topic, provisioningResultPayload, getRestBaseUrl(), parameters.get(3));
        }
        catch (Exception e) {
            log.error("Failed to transmit the message to CCW", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // CUST Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitCustActivationRequest(Message<?> message) {

        String topic = null;
        List<String> parameters = null;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            CustPayload custPayload = convertPayloadToObject(message.getPayload(), CustPayload.class);

            // Session 정보 저장
            // ------------------------------------------------------------------------------- //
            String sessionId = custPayload.getHeader().getUserProperties().getSessionId();
            sessionManage.setSessionId(parameters.get(0), MessageInfo.ServiceEnum.CUST.toString(), sessionId);

            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.CUST.toString(), custPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId == null) {

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    message.getPayload().toString(),    // Payload
                    ServiceLogTypes.CUST,               // 서비스 Type
                    ServiceLogSystems.DCM,              // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                message.getPayload().toString(),                            // Payload
                teleSvcMainId,                                              // 마스터 로그 식별 ID
                ServiceLogSystems.DCM,                                      // 출발지
                ServiceLogSystems.MQTT,                                     // 목적지
                true,                                            // 결과
                ServiceLogContents.CUST_PROVISIONING_REQUEST_DCM_TO_MQTT    // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Topic 및 Payload 전달
            //sendPayload(topic, custPayload, getRestBaseUrl(), parameters.get(3));

            // Provisioning 정보를 DB에서 확인하여 바로 요청 처리
            handleCustProvisioningRequest(parameters, MessageInfo.TypeEnum.ACK);
        }
        catch (Exception e) {
            log.error("Failed to transmit the message to CCW", e);
            nackCustProvisioningRequest(message, MessageInfo.TypeEnum.NACK);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // Cust Provisioning Request
    // ------------------------------------------------------------------------------------------------ //
    private void handleCustProvisioningRequest(List<String> parameters, MessageInfo.TypeEnum messageType) {

        try {
            CustProvisioningPayload custProvisioningConfigurationPayload = new CustProvisioningPayload();

            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            String sessionId = sessionManage.getSessionid(parameters.get(0), MessageInfo.ServiceEnum.CUST.toString());
            custProvisioningConfigurationPayload.getHeader().getUserProperties().setSessionId(sessionId);

            custProvisioningConfigurationPayload.getHeader().getUserProperties().setCorrelationId(sessionManage.getCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.CUST.toString()));
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
            CustProvisioningPayloadBody bodySetting = new CustProvisioningPayloadBody();

            /*
            // reportSetting > interval
            bodySetting.getReportSetting().getInterval().setUnit("DAYS");
            bodySetting.getReportSetting().getInterval().setValue(1);

            // provisioning > serviceFlags
            List<ServiceFlag> serviceFlags = Arrays.asList(
                new ServiceFlag(ServiceFlag.ServiceEnum.ACN, ServiceFlag.FlagValueEnum.ON),
                new ServiceFlag(ServiceFlag.ServiceEnum.SOS, ServiceFlag.FlagValueEnum.ON),
                new ServiceFlag(ServiceFlag.ServiceEnum.VLS, ServiceFlag.FlagValueEnum.ON),
                new ServiceFlag(ServiceFlag.ServiceEnum.RSN, ServiceFlag.FlagValueEnum.ON),
                new ServiceFlag(ServiceFlag.ServiceEnum.DHC, ServiceFlag.FlagValueEnum.ON)
            );
            bodySetting.getProvisioning().setServiceFlags(serviceFlags);

            // provisioning > brand
            bodySetting.getProvisioning().setBrand("Toyota");

            // provisioning > provisioningLanguage
            bodySetting.getProvisioning().setProvisioningLanguage("en");

            // provisioning > configuration > phoneNumbers
            List<PhoneNumber> phoneNumbers = Arrays.asList(
                new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.PRIMARY, "+821050820850"),
                new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.SECONDARY, "+821042350309"),
                new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.PRIMARY, "+821050820850"),
                new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.SECONDARY, "+821042350309"),
                new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.PRIMARY, "+821050820850"),
                new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.SECONDARY, "+821042350309"),
                new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.PRIMARY, "+821050820850"),
                new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.SECONDARY, "+821042350309"),
                new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.PRIMARY, "+821050820850"),
                new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.SECONDARY, "+821042350309")

                //new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.PRIMARY, "+820269017801"),
                //new PhoneNumber(PhoneNumber.ServiceEnum.ACN, PhoneNumber.TypeEnum.SECONDARY, "+820269017802"),
                //new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.PRIMARY, "+820269017803"),
                //new PhoneNumber(PhoneNumber.ServiceEnum.SOS, PhoneNumber.TypeEnum.SECONDARY, "+820269017804"),
                //new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.PRIMARY, "+820269017805"),
                //new PhoneNumber(PhoneNumber.ServiceEnum.RSN, PhoneNumber.TypeEnum.SECONDARY, "+820269017806"),
                //new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.PRIMARY, "+820269017807"),
                //new PhoneNumber(PhoneNumber.ServiceEnum.VLS, PhoneNumber.TypeEnum.SECONDARY, "+820269017808"),
                //new PhoneNumber(PhoneNumber.ServiceEnum.INBOUND, PhoneNumber.TypeEnum.PRIMARY, "+820269017809")
            );
            bodySetting.getProvisioning().getConfiguration().setPhoneNumbers(phoneNumbers);

            // provisioning > configuration > callbackStandByTimer
            bodySetting.getProvisioning().getConfiguration().setCallbackStandByTimer(1);

            // provisioning > configuration > sosCancelTimer
            bodySetting.getProvisioning().getConfiguration().setSosCancelTimer(0);
            */

            DhcIntervalFinalEntity dhcIntervalFinalEntity = mqttService.getDhcIntervalFinalByVin(parameters.get(0));

            // DHC Interval 설정 정보가 없는 경우, 설정 정보 생성
            if(dhcIntervalFinalEntity == null) {

                mqttService.insertDefaultPreset("D", parameters.get(0));
//                mqttService.insertDhcIntervalFinal(parameters.get(0));
                dhcIntervalFinalEntity = mqttService.getDhcIntervalFinalByVin(parameters.get(0));

                mqttService.insertDhcIntervalHistory(parameters.get(0));
            }

            bodySetting.getReportSetting().getInterval().setUnit("DAYS");
            bodySetting.getReportSetting().getInterval().setValue(dhcIntervalFinalEntity.getIntervalValue());

            ProvisioningFinalEntity provisioningFinalEntity = mqttService.getProvisioningFinalByVin(parameters.get(0));

            // Provisioning 설정 정보가 없는 경우, 설정 정보 생성
            if(provisioningFinalEntity == null) {
                mqttService.insertDefaultPreset("P", parameters.get(0));
//                mqttService.insertProvisioningFinal(parameters.get(0));
                provisioningFinalEntity = mqttService.getProvisioningFinalByVin(parameters.get(0));

                mqttService.insertProvisioningHistory(parameters.get(0));
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

            custProvisioningConfigurationPayload.setBody(bodySetting);
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    objectMapper.writeValueAsString(custProvisioningConfigurationPayload),  // Payload
                    teleSvcMainId,                                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.MQTT,                                                 // 출발지
                    ServiceLogSystems.DCM,                                                  // 목적지
                    true,                                                        // 결과
                    ServiceLogContents.CUST_PROVISIONING_REQUEST_SEND_MQTT_TO_DCM           // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Publishing Command to Broker
            String topic = publisherService.receiveCustProvisioningConfiguration(custProvisioningConfigurationPayload, parameters.get(0), parameters.get(1), parameters.get(2));

            // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
            //sendPayload(topic, custProvisioningConfigurationPayload, getRestBaseUrl(), "response");
        }
        catch (Exception e) {
            log.error("Failed to publish the command to DCM", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // NACK Cust Provisioning Request
    // In the event of a NACK from the TSP, the body element will not be sent.
    // ------------------------------------------------------------------------------------------------ //
    private void nackCustProvisioningRequest(Message<?> message, MessageInfo.TypeEnum messageType) {

        String topic = null;
        List<String> parameters = null;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            String payload = (String) message.getPayload();

            JsonNode payloadNode = convertJsonToJsonNode(payload);

            String correlationId = payloadNode.at("/header/userProperties/correlationId").asText();
            String sessionId = payloadNode.at("/header/userProperties/sessionId").asText();

            if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {

                CustProvisioningPayload custProvisioningConfigurationPayload = new CustProvisioningPayload();

                // userProperties 설정
                // ------------------------------------------------------------------------------- //
                custProvisioningConfigurationPayload.getHeader().getUserProperties().setCorrelationId(correlationId);
                custProvisioningConfigurationPayload.getHeader().getUserProperties().setSessionId(sessionId);
                // ------------------------------------------------------------------------------- //

                // message 설정
                // ------------------------------------------------------------------------------- //
                custProvisioningConfigurationPayload.getHeader().getMessage().setType(messageType);
                custProvisioningConfigurationPayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.CUST);
                custProvisioningConfigurationPayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.CUST);
                // ------------------------------------------------------------------------------- //

                // transmissionTimestampUTC 설정
                custProvisioningConfigurationPayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

                // In the event of a NACK from the TSP, the body element will not be sent.
                custProvisioningConfigurationPayload.setBody(null);

                // Service Transaction Detail Log 기록
                // ------------------------------------------------------------------------------- //
                Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

                if(teleSvcMainId != null) {

                    saveServiceDetailLog(
                        objectMapper.writeValueAsString(custProvisioningConfigurationPayload),  // Payload
                        teleSvcMainId,                                                          // 마스터 로그 식별 ID
                        ServiceLogSystems.MQTT,                                                 // 출발지
                        ServiceLogSystems.DCM,                                                  // 목적지
                        false,                                                        // 결과
                        ServiceLogContents.CUST_PROVISIONING_REQUEST_SEND_MQTT_TO_DCM           // 로그내용
                    );
                }
                // ------------------------------------------------------------------------------- //

                // Publishing Command to Broker
                String responTopic = publisherService.receiveCustProvisioningConfiguration(custProvisioningConfigurationPayload, parameters.get(0), parameters.get(1), parameters.get(2));

                // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
                //sendPayload(responTopic, custProvisioningConfigurationPayload, getRestBaseUrl(), "response");

            }
            else {
                log.error("Failed to retrieve sessionId & correlationId");
            }
        }
        catch (Exception e) {
            log.error("Failed to publish response", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // CUST Provioning Result Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handlePublishCustResult(Message<?> message) {

        String topic = null;
        List<String> parameters = null;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            CustResultPayload custResultPayload = convertPayloadToObject(message.getPayload(), CustResultPayload.class);

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            String sessionId = custResultPayload.getHeader().getUserProperties().getSessionId();

            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                            // Payload
                    teleSvcMainId,                                              // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                                      // 출발지
                    ServiceLogSystems.MQTT,                                     // 목적지
                    true,                                            // 결과
                    ServiceLogContents.CUST_ACTIVATION_RESULT_SEND_DCM_TO_MQTT  // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Topic 및 Payload 전달
            //sendPayload(topic, custResultPayload, getRestBaseUrl(), parameters.get(3));
        }
        catch (Exception e) {
            log.error("Failed to transmit the message to CCW", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // DHC Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitHealthCheckEvent(Message<?> message) {

        String topic = null;
        List<String> parameters = null;
        MessageInfo.TypeEnum messageType = MessageInfo.TypeEnum.ACK;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            DhcPayload dhcPayload = convertPayloadToObject(message.getPayload(), DhcPayload.class);

            // Session 정보 저장
            // ------------------------------------------------------------------------------- //
            String sessionId = dhcPayload.getHeader().getUserProperties().getSessionId();
            sessionManage.setSessionId(parameters.get(0), MessageInfo.ServiceEnum.DHC.toString(), sessionId);

            sessionManage.setCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.DHC.toString(), dhcPayload.getHeader().getUserProperties().getCorrelationId());
            // ------------------------------------------------------------------------------- //

            // Service Transaction Main Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId == null) {

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    message.getPayload().toString(),    // Payload
                    ServiceLogTypes.DHC_REPORT,         // 서비스 Type
                    ServiceLogSystems.DCM,              // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                message.getPayload().toString(),                    // Payload
                teleSvcMainId,                                      // 마스터 로그 식별 ID
                ServiceLogSystems.DCM,                              // 출발지
                ServiceLogSystems.MQTT,                             // 목적지
                true,                                    // 결과
                ServiceLogContents.DHC_REPORT_SEND_DCM_TO_MQTT      // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Topic 및 Payload 전달
            //sendPayload(topic, dhcPayload, getRestBaseUrl(), parameters.get(3));
        }
        catch (Exception e) {
            messageType = MessageInfo.TypeEnum.NACK;
            log.error("Failed to transmit the message to CCW", e);
        }

        // Response 송신
        handleDhcResponse(message, parameters, messageType);
    }
    // ------------------------------------------------------------------------------------------------ //


    // DHC Response Message를 DCM으로 송신
    // ------------------------------------------------------------------------------------------------ //
    private void handleDhcResponse(Message<?> message, List<String> parameters, MessageInfo.TypeEnum messageType) {

        boolean resultFlag = true;
        String correlationId = "", sessionId = "";
        String topic = null;
        DhcResponsePayload dhcResponsePayload = new DhcResponsePayload();

        try {
            if(messageType == MessageInfo.TypeEnum.ACK) {

                // Session ID 및 Correlation ID 유지
                // ----------------------------------------------------------------------------------- //
                correlationId = sessionManage.getCorrelationId(parameters.get(0), MessageInfo.ServiceEnum.DHC.toString());
                sessionId = sessionManage.getSessionid(parameters.get(0), MessageInfo.ServiceEnum.DHC.toString());
                // ----------------------------------------------------------------------------------- //

                if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {

                    // userProperties 설정
                    // ------------------------------------------------------------------------------- //
                    dhcResponsePayload.getHeader().getUserProperties().setCorrelationId(correlationId);
                    dhcResponsePayload.getHeader().getUserProperties().setSessionId(sessionId);
                    // ------------------------------------------------------------------------------- //

                    // message 설정
                    // ------------------------------------------------------------------------------- //
                    dhcResponsePayload.getHeader().getMessage().setType(messageType);
                    dhcResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.DHC);
                    dhcResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.DHC);
                    // ------------------------------------------------------------------------------- //

                    // transmissionTimestampUTC 설정
                    dhcResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

                    // Payload Body 설정
                    // ------------------------------------------------------------------------------- //
                    DhcResponsePayloadBody dhcResponsePayloadBody = new DhcResponsePayloadBody();

                    DhcIntervalFinalEntity dhcIntervalFinalEntity = mqttService.getDhcIntervalFinalByVin(parameters.get(0));

                    // DHC Interval 설정 정보가 없는 경우는 테스트 상황으로 판단되므로 해당 DB Record를 생성하지는 않음
                    if(dhcIntervalFinalEntity == null) {
                        dhcResponsePayloadBody.getInterval().setUnit("DAYS");
                        dhcResponsePayloadBody.getInterval().setValue(1);
                    }
                    else {
                        dhcResponsePayloadBody.getInterval().setUnit("DAYS");
                        dhcResponsePayloadBody.getInterval().setValue(dhcIntervalFinalEntity.getIntervalValue());
                    }

                    dhcResponsePayload.setBody(dhcResponsePayloadBody);
                    // ------------------------------------------------------------------------------- //

                    // Publishing Response to Broker
                    topic = publisherService.receiveDhcResponse(dhcResponsePayload, parameters.get(0), parameters.get(1), parameters.get(2));

                    // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
                    //sendPayload(topic, dhcResponsePayload, getRestBaseUrl(), "response");
                    // ------------------------------------------------------------------------------- //
                }
                else {
                    log.error("Failed to retrieve sessionId & correlationId");
                    resultFlag = false;
                }
            }
            else {
                // Topic 정보 추출
                topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

                // parameters 정보 추출
                parameters = decodeTopic(topic);

                String payload = (String) message.getPayload();

                JsonNode payloadNode = convertJsonToJsonNode(payload);

                correlationId = payloadNode.at("/header/userProperties/correlationId").asText();
                sessionId = payloadNode.at("/header/userProperties/sessionId").asText();

                if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {

                    // userProperties 설정
                    // ------------------------------------------------------------------------------- //
                    dhcResponsePayload.getHeader().getUserProperties().setCorrelationId(correlationId);
                    dhcResponsePayload.getHeader().getUserProperties().setSessionId(sessionId);
                    // ------------------------------------------------------------------------------- //

                    // message 설정
                    // ------------------------------------------------------------------------------- //
                    dhcResponsePayload.getHeader().getMessage().setType(messageType);
                    dhcResponsePayload.getHeader().getMessage().setService(MessageInfo.ServiceEnum.DHC);
                    dhcResponsePayload.getHeader().getMessage().setOperation(MessageInfo.OperationEnum.DHC);
                    // ------------------------------------------------------------------------------- //

                    // transmissionTimestampUTC 설정
                    dhcResponsePayload.getHeader().setTransmissionTimestampUTC(sessionManage.getCurrentOffsetDateTime());

                    //Body containing DHC reporting configuration. In the event of a NACK from the TSP, the body element will not be sent.
                    dhcResponsePayload.setBody(null);
                    // ------------------------------------------------------------------------------- //

                    // Publishing Response to Broker
                    String respontopic = publisherService.receiveDhcResponse(dhcResponsePayload, parameters.get(0), parameters.get(1), parameters.get(2));

                    // G/W 응답 전달 (CCW Simulator 화면 표출 목적으로 테스트 이후 불필요)
                    //sendPayload(respontopic, dhcResponsePayload, getRestBaseUrl(), "response");
                    // ------------------------------------------------------------------------------- //    
                }
                else {
                    log.error("Failed to retrieve sessionId & correlationId");
                    resultFlag = false;
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to publish the response to DCM", e);
            resultFlag = false;
        }

        // Service Transaction Detail Log 기록
        // ----------------------------------------------------------------------------------- //
        if (StringUtils.hasText(correlationId) && StringUtils.hasText(sessionId)) {

            try {
                Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

                if(teleSvcMainId != null) {

                    saveServiceDetailLog(
                        objectMapper.writeValueAsString(dhcResponsePayload),    // Payload
                        teleSvcMainId,                                          // 마스터 로그 식별 ID
                        ServiceLogSystems.MQTT,                                 // 출발지
                        ServiceLogSystems.DCM,                                  // 목적지
                        resultFlag,                                             // 결과
                        ServiceLogContents.DHC_REPORT_RESPONSE_MQTT_TO_DCM      // 로그내용
                    );

                    if(!resultFlag) {
                        mqttService.updateStatusAndTime(ServiceLogStatusCode.FAILURE, LocalDateTime.now(), sessionId);
                    }
                }
            }
            catch (JsonProcessingException e) {
                log.error("Failed to send response", e);
            }
        }
        // ----------------------------------------------------------------------------------- //
    }
    // ------------------------------------------------------------------------------------------------ //


    // DHC Error Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitDhcErrorEvent(Message<?> message) {
        handleEmitErrorEvent(message, MessageInfo.ServiceEnum.DHC);
    }
    // ------------------------------------------------------------------------------------------------ //


    // Voice Kill Command : VCT ACK인 경우, DCM 오작동으로 인한 Call 재개 방지를 위해 VoiceKill 송신
    // ------------------------------------------------------------------------------------------------ //
    private void handleVoiceKillRequest(List<String> parameters, MessageInfo.ServiceEnum messageService) {

        try {
            VoicekillPayload voicekillCommandPayload = new VoicekillPayload();
            CommonHeader header = new CommonHeader();

            // userProperties 설정
            // ------------------------------------------------------------------------------- //
            String sessionId = sessionManage.getSessionid(parameters.get(0), messageService.toString());
            header.getUserProperties().setSessionId(sessionId);

            header.getUserProperties().setCorrelationId(sessionManage.getCorrelationId(parameters.get(0), messageService.toString()));
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
                else if(messageService == MessageInfo.ServiceEnum.VLS) {
                    serviceLogType = ServiceLogTypes.VLS;
                }

                SvcMainLogEntity svcMainLogEntity = saveServiceMainLog(
                    objectMapper.writeValueAsString(voicekillCommandPayload),   // Payload
                    serviceLogType,                                       // 서비스 Type
                    ServiceLogSystems.MQTT,                                      // 요청 소스
                    sessionId
                );

                teleSvcMainId = svcMainLogEntity.getTeleSvcMainId();
            }
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            saveServiceDetailLog(
                objectMapper.writeValueAsString(voicekillCommandPayload),   // Payload
                teleSvcMainId,                                              // 마스터 로그 식별 ID
                ServiceLogSystems.MQTT,                                     // 출발지
                ServiceLogSystems.DCM,                                      // 목적지
                true,                                            // 결과
                ServiceLogContents.VOICEKILL_REQUEST_MQTT_TO_DCM            // 로그내용
            );
            // ------------------------------------------------------------------------------- //

            // Publishing Command to Broker
            String topic = publisherService.receiveVoiceKillCommandMessage(voicekillCommandPayload, parameters.get(0), parameters.get(1), parameters.get(2));
        }
        catch (Exception e) {
            log.error("Failed to publish the command to DCM", e);
        }
    }
    // -------------------------------------------------------------------------------------------- //


    // Voice Kill Result Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitVoiceKillResult(Message<?> message) {

        String topic = null;
        List<String> parameters = null;

        try {
            // Topic 정보 추출
            topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // parameters 정보 추출
            parameters = decodeTopic(topic);

            // Payload 설정
            VoicekillResultPayload voicekillCommandResultPayload = convertPayloadToObject(message.getPayload(), VoicekillResultPayload.class);

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            String sessionId = voicekillCommandResultPayload.getHeader().getUserProperties().getSessionId();
            // ------------------------------------------------------------------------------- //

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                    // Payload
                    teleSvcMainId,                                      // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                              // 출발지
                    ServiceLogSystems.MQTT,                             // 목적지
                    true,                                    // 결과
                    ServiceLogContents.VOICEKILL_RESPONSE_DCM_TO_MQTT   // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Topic 및 Payload 전달
            //sendPayload(topic, voicekillCommandResultPayload, getRestBaseUrl(), parameters.get(3));
        }
        catch (Exception e) {
            log.error("Failed to transmit the message to CCW", e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // Error Message를 CCW로 전달
    // ------------------------------------------------------------------------------------------------ //
    private void handleEmitErrorEvent(Message<?> message, MessageInfo.ServiceEnum serviceType) {

        String topic = null;

        try {
            // Topic 정보 추출
            //topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

            // Payload 설정
            ErrorPayload errorPayload = convertPayloadToObject(message.getPayload(), ErrorPayload.class);

            // errorPayload는 Session 정보 저장 안하는 것으로

            // Service Transaction Detail Log 기록
            // ------------------------------------------------------------------------------- //
            String serviceLogContent = "";

            if(serviceType == MessageInfo.ServiceEnum.DHC) {
                serviceLogContent = ServiceLogContents.DHC_COMMAND_ERROR_SEND_MQTT_TO_DCM;
            }
            else {
                serviceLogContent = ServiceLogContents.RETRANSMIT_ERROR_SEND_DCM_TO_MQTT;
            }

            String sessionId = errorPayload.getHeader().getUserProperties().getSessionId();

            Long teleSvcMainId = mqttService.getTeleSvcMainIdBySessionId(sessionId);

            if(teleSvcMainId != null) {

                saveServiceDetailLog(
                    message.getPayload().toString(),                        // Payload
                    teleSvcMainId,                                          // 마스터 로그 식별 ID
                    ServiceLogSystems.DCM,                                  // 출발지
                    ServiceLogSystems.MQTT,                                 // 목적지
                    true,                                        // 결과
                    serviceLogContent                                       // 로그내용
                );
            }
            // ------------------------------------------------------------------------------- //

            // Topic 및 Payload 전달
            //sendPayload(topic, errorPayload, getRestBaseUrl(), "error");

            // ECall에 대해서만 Error 전달 (To CTI)
            // ------------------------------------------------------------------------------- //
            if(serviceType == MessageInfo.ServiceEnum.ACN || serviceType == MessageInfo.ServiceEnum.SOS || serviceType == MessageInfo.ServiceEnum.RSN) {
                topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

                // ECall Topic 추출 위해 error 문자열 제거
                topic = topic.substring(0, topic.lastIndexOf("/"));
    
                sendPayload(topic, errorPayload, getRestBaseUrl(), serviceType.toString().toLowerCase());
            }
            // ------------------------------------------------------------------------------- //
        }
        catch (Exception e) {
            log.error("Failed to transmit the message to CCW", e);
        }

        /* Error Message는 CCW로부터의 요청이 Prority나 Case에 맞지 않는 경우 발생하는 것으로 Response 불필요함 */
    }
    // ------------------------------------------------------------------------------------------------ //


    // Payload 송신
    // ------------------------------------------------------------------------------------------------ //
    private <T> MessageInfo.TypeEnum sendPayload(String topic, T payload, String baseUrl, String subUrl) {

        String url = baseUrl + subUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.add("mqtt-topic", topic);

        try {
            String jsonString = objectMapper.writeValueAsString(payload);
            JsonNode rootNode = objectMapper.readTree(jsonString);
                            
            // 최상위 노드의 두 번째 필드명 가져오기
            Iterator<String> fieldNames = rootNode.get("body").fieldNames();

            // 두 번째 노드 찾기
            String secondNodeName = null;
            if (fieldNames.hasNext()) {
                secondNodeName = fieldNames.next();
            }
            
            if (StringUtils.hasText(secondNodeName) && secondNodeName.equals("vehicleReport")) {

                JsonNode gpsDataNode = rootNode.path("body").path("vehicleReport").path("gpsData");

                // lat, lon 값 변환 작업업
                if (!gpsDataNode.isMissingNode() && gpsDataNode.has("coordinate")) {
    
                    JsonNode coordinateNode = gpsDataNode.path("coordinate");
        
                    String latMas = coordinateNode.path("lat").asText();
                    String lonMas = coordinateNode.path("lon").asText();
    
                    if (!latMas.equalsIgnoreCase("UNKNOWN") && !lonMas.equalsIgnoreCase("UNKNOWN")) {
                        
                        String convertedLat = ConvertUilts.convertMasToLatitude(latMas);
                        String convertedLon = ConvertUilts.convertMasToLongitude(lonMas);
    
                        ((ObjectNode) coordinateNode).put("lat", convertedLat);
                        ((ObjectNode) coordinateNode).put("lon", convertedLon);
                    }
                }
                
                // 속도 변환 작업
                if (!gpsDataNode.isMissingNode() && gpsDataNode.has("velocity")) {

                    JsonNode velocityNode = gpsDataNode.path("velocity");

                    String unit = velocityNode.path("unit").asText();
                    int value = velocityNode.path("value").asInt();

                    if (StringUtils.hasText(unit) && unit.equals("MPH")) {
                        ((ObjectNode) velocityNode).put("unit", "KPH");
                        int kph = 0;
                        if (value > 0) {
                            kph = (int) Math.round(value * 1.60934);

                            ((ObjectNode) velocityNode).put("value", kph);
                        }
                    }
                }

                String updatedPayloadJson = objectMapper.writeValueAsString(rootNode);
                payload = objectMapper.readValue(updatedPayloadJson, (Class<T>) payload.getClass());

            }
            else if  (StringUtils.hasText(secondNodeName) && secondNodeName.equals("dhc")) {
                
                JsonNode gpsDataNode = rootNode.path("body").path("dhc").path("gpsData");

                if (!gpsDataNode.isMissingNode() && gpsDataNode.has("coordinate")) {

                    JsonNode coordinateNode = gpsDataNode.path("coordinate");
        
                    String latMas = coordinateNode.path("lat").asText();
                    String lonMas = coordinateNode.path("lon").asText();

                    if (!latMas.equalsIgnoreCase("UNKNOWN") && !lonMas.equalsIgnoreCase("UNKNOWN")) {
                        
                        String convertedLat = ConvertUilts.convertMasToLatitude(latMas);
                        String convertedLon = ConvertUilts.convertMasToLongitude(lonMas);

                        ((ObjectNode) coordinateNode).put("lat", convertedLat);
                        ((ObjectNode) coordinateNode).put("lon", convertedLon);
                    }
                }

                // 속도 변환 작업
                if (!gpsDataNode.isMissingNode() && gpsDataNode.has("velocity")) {

                    JsonNode velocityNode = gpsDataNode.path("velocity");

                    String unit = velocityNode.path("unit").asText();
                    int value = velocityNode.path("value").asInt();

                    if (StringUtils.hasText(unit) && unit.equals("MPH")) {
                        ((ObjectNode) velocityNode).put("unit", "KPH");
                        int kph = 0;
                        if (value > 0) {
                            kph = (int) Math.round(value * 1.60934);

                            ((ObjectNode) velocityNode).put("value", kph);
                        }
                    }
                }

                String updatedPayloadJson = objectMapper.writeValueAsString(rootNode);
                payload = objectMapper.readValue(updatedPayloadJson, (Class<T>) payload.getClass());
            }
            
            HttpEntity<T> entity = new HttpEntity<>(payload, headers);

            log.info("REST Request : {} {}", url, topic);
            ResponseEntity<Void> res = new RestTemplate().postForEntity(url, entity, Void.class);
            log.info("REST Response : {}", res.getStatusCode());

            // 송신 결과 확인
            if (!res.getStatusCode().is2xxSuccessful()) {
                return MessageInfo.TypeEnum.NACK;
            }
    
            return MessageInfo.TypeEnum.ACK;
        } 
        catch (Exception e) {
            log.error("Failed to send payload: {}", e.getMessage(), e);
            return MessageInfo.TypeEnum.NACK;
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // 지정된 Class로 Mapping
    // ------------------------------------------------------------------------------------------------ //
    private <T> T convertPayloadToObject(Object payload, Class<T> targetType) {

        if (payload == null) {
            log.warn("Payload is null, cannot convert to object.");
            throw new IllegalArgumentException("Payload cannot be null.");
        }

        if (!(payload instanceof String)) {
            log.warn("Payload is not of type String, cannot convert to object. Type: {}", payload.getClass().getName());
            throw new IllegalArgumentException("Payload is not of type String.");
        }

        String payloadStr = ((String) payload).trim();

        try {
            // JSON 변환 및 유효성 검증
            return objectMapper.readValue(payloadStr, targetType);
        }
        catch (JsonProcessingException e) {
            log.error("Failed to parse JSON: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid JSON payload: " + e.getMessage(), e);
        }
        catch (Exception e) {
            log.error("Unexpected error during payload conversion: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert payload to object: " + e.getMessage(), e);
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // Json 변환
    // ------------------------------------------------------------------------------------------------ //
    public JsonNode convertJsonToJsonNode(String payloadStr) throws JsonProcessingException {

        if (payloadStr instanceof String) {

            try {
                // JSON 형식 확인 후 변환
                if (payloadStr.trim().startsWith("{")) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.readTree(payloadStr);
                }
                else {
                    log.info("Payload is not in the expected JSON format.");
                    throw new IllegalArgumentException("Payload is not in the expected JSON format.");
                }
            }
            catch (Exception e) {
                //e.printStackTrace();
                log.info("Failed to convert payload to object. {}", e);
                throw new RuntimeException("Failed to convert payload to object", e);
            }
        }

        return null;
    }
    // ------------------------------------------------------------------------------------------------ //


    // Topic에서 '+' 패턴 위치의 문자열 추출
    // ------------------------------------------------------------------------------------------------ //
    private List<String> decodeTopic(String topic) {

        List<String> parameters = new ArrayList<>();

        String[] parts = topic.split("/");

        // deviceId, ecuId, appId 추출
        for (int position : PARAMS_POSITIONS) {

            if (position >= 0 && position < parts.length) {
                parameters.add(parts[position]);
            } else {
                log.info("Position " + position + " is out of bounds for topic: " + topic);
            }
        }

        // service 추출
        if (!PARAMS_POSITIONS.isEmpty()) {

            int startPosition = PARAMS_POSITIONS.get(PARAMS_POSITIONS.size() - 1) + 1;

            if (startPosition < parts.length) {

                StringBuilder camelCaseBuilder = new StringBuilder();

                for (int i = startPosition; i < parts.length; i++) {

                    String part = parts[i].toLowerCase();

                    if (i != startPosition) {
                        part = part.substring(0, 1).toUpperCase() + part.substring(1);
                    }

                    camelCaseBuilder.append(part);
                }

                parameters.add(camelCaseBuilder.toString());
            }
        }

        return parameters;
    }
    // ------------------------------------------------------------------------------------------------ //


    // Async 처리를 위한
    // ------------------------------------------------------------------------------------------------ //
    ExecutorService executorService = new ThreadPoolExecutor(
            10,                         // Core pool size
            30,                     // Maximum pool size
            60L, TimeUnit.SECONDS,      // keepAliveTime (유휴 스레드 유지 시간)
            new LinkedBlockingQueue<>(100)  // Queue size
    );
    // ------------------------------------------------------------------------------------------------ //


    // DCM으로부터 수신한 메시지 처리
    // ------------------------------------------------------------------------------------------------ //
    public void handleReceivedMessage(Message<?> message) {

        try {
            // Message Queue에 저장
            messageQueue.put(message);

            log.info("Message queue size: {}", messageQueue.size());
        }
        catch (InterruptedException e) {
            log.error("Failed to queue message", e);
            Thread.currentThread().interrupt();
        }
    }

    private void startProcessingMessages() {

        executorService.submit(() -> {

            while (true) {
                try {
                    Message<?> message = messageQueue.take();

                    // Thread를 통한 병렬 처리
                    executorService.submit(() -> processMessage(message));
                }
                catch (InterruptedException e) {
                    log.error("Message processing thread interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private void processMessage(Message<?> message) {

        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);

        if (topic == null) {
            log.warn("Received the message with null topic");
            return;
        }

        log.info("Processing the message from topic: {}", topic);

        boolean matched = false;
        for (Map.Entry<Pattern, Consumer<Message<?>>> entry : topicHandlers.entrySet()) {
            if (entry.getKey().matcher(topic).matches()) {
                try {
                    entry.getValue().accept(message);
                    matched = true;
                    break;
                }
                catch (Exception e) {
                    log.error("processing the message for topic {}: {}", topic, e.getMessage(), e);
                }
            }
        }

        if (!matched) {
            log.warn("Received the message on unknown topic: {}", topic);
        }
    }

    private void shutdown() {

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }
        catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
    // ------------------------------------------------------------------------------------------------ //


    // 차량연동서비스 마스터 로그 저장
    // ------------------------------------------------------------------------------------------------ //
    private SvcMainLogEntity saveServiceMainLog(String strMessage, String serviceLogType, String requestSource, String sessionId) {

        SvcMainLogEntity svcMainLogEntity = new SvcMainLogEntity();

        try {
            JsonNode jsonNode = objectMapper.readTree(strMessage);

            // Session ID
            svcMainLogEntity.setSessionId(sessionId);

            // DCM VIN
            String vin = jsonNode.path("header").path("device").path("VIN").asText();
            svcMainLogEntity.setVin(vin);

            // MSISDN
            String msin = jsonNode.path("header").path("device").path("MSISDN").asText();
            if (msin.startsWith("82")) {
                msin = "0" + msin.substring(2);
            }
            svcMainLogEntity.setMsin(msin);

            // IMEI
            String imei = jsonNode.path("header").path("device").path("IMEI").asText();
            svcMainLogEntity.setImei(imei);

            // ICCID
            String iccid = jsonNode.path("header").path("device").path("ICCID").asText();
            svcMainLogEntity.setIccid(iccid);

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
        catch (JsonProcessingException e) {
            log.error("JSON 처리 중 오류 발생: {}", e.getMessage(), e);
        }
        catch (Exception e) {
            log.error("처리 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
        }

        return svcMainLogEntity;
    }
    // ------------------------------------------------------------------------------------------------ //


    // 차량연동서비스 상세 로그 저장
    // ------------------------------------------------------------------------------------------------ //
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

            // error 확인
            boolean hasBody = rootNode.has("body");
            boolean hasBodyCode = hasBody && rootNode.path("body").has("code");
            boolean hasErrorCode = false;

            if (hasBody && rootNode.path("body").has("error")) {
                hasErrorCode = rootNode.path("body").path("error").has("code");
            }

            // body > code 형태인 경우
            if (hasBodyCode) {
                int errorCode = rootNode.path("body").path("code").asInt();
                svcDetailLogEntity.setNackCode(errorCode);
            }
            // body > error > code 형태인 경우
            else if (hasErrorCode) {
                int errorCode = rootNode.path("body").path("error").path("code").asInt();
                svcDetailLogEntity.setNackCode(errorCode);
            }
        }
        catch (JsonProcessingException e) {
            log.error("처리 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
        }

        mqttService.insertSvcDetailLog(svcDetailLogEntity);
    }
    // ------------------------------------------------------------------------------------------------ //


    // REST API 기본 Url
    // ------------------------------------------------------------------------------------------------ //
    private String getRestBaseUrl() {

        String url = "";

        if(PROFILE_ACTIVE.equals("local") || PROFILE_ACTIVE.equals("dev")){
            url = SPRING_CCB_URI + ":" + SPRING_CCB_PORT + "/cc/24dcm/";
        } else if(PROFILE_ACTIVE.equals("prod") || PROFILE_ACTIVE.equals("sta")){
            url = CCB_URL + ":" + CCB_PORT + "/cc/24dcm/";
        }

        return url;
    }
    // ------------------------------------------------------------------------------------------------ //
}
