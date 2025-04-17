package com.engistech.gateway.service.impl;

import com.engistech.gateway.model.acn.AcnResponsePayload;
import com.engistech.gateway.model.acn.AcnVctResponsePayload;
import com.engistech.gateway.model.common.RetransmitPayload;
import com.engistech.gateway.model.dhc.DhcCommandPayload;
import com.engistech.gateway.model.dhc.DhcResponsePayload;
import com.engistech.gateway.model.provisioning.CustProvisioningPayload;
import com.engistech.gateway.model.provisioning.ProvisioningPayload;
import com.engistech.gateway.model.rsn.RsnResponsePayload;
import com.engistech.gateway.model.rsn.RsnVctResponsePayload;
import com.engistech.gateway.model.sos.SosResponsePayload;
import com.engistech.gateway.model.sos.SosVctResponsePayload;
import com.engistech.gateway.model.vls.VlsResponsePayload;
import com.engistech.gateway.model.vls.VlsStartPayload;
import com.engistech.gateway.model.vls.VlsStopPayload;
import com.engistech.gateway.model.vls.VlsVctResponsePayload;
import com.engistech.gateway.model.vls.VlsVoiceCallPayload;
import com.engistech.gateway.model.vls.VoicekillPayload;
import com.engistech.gateway.service.MqttMessagePublisher;
import com.engistech.gateway.service.PublisherService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.processing.Generated;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Generated(value="com.cmm.asyncapi.generator.template.spring", date="2024-11-01T09:28:18.133Z")
@Service
public class PublisherServiceImpl implements PublisherService {

    @Value("${mqtt.topic.receiveAcnResponse}")
    private String receiveAcnResponseTopic;

    // @Autowired
    // private MessageHandler receiveAcnResponseOutbound;
    @Value("${mqtt.topic.receiveAcnVctResponse}")
    private String receiveAcnVctResponseTopic;

    // @Autowired
    // private MessageHandler receiveAcnVctResponseOutbound;
    @Value("${mqtt.topic.receiveAcnRetransmitCommandMessage}")
    private String receiveAcnRetransmitCommandMessageTopic;

    // @Autowired
    // private MessageHandler receiveAcnRetransmitCommandMessageOutbound;
    @Value("${mqtt.topic.receiveSosResponse}")
    private String receiveSosResponseTopic;

    // @Autowired
    // private MessageHandler receiveSosResponseOutbound;
    @Value("${mqtt.topic.receiveSosVctResponse}")
    private String receiveSosVctResponseTopic;

    // @Autowired
    // private MessageHandler receiveSosVctResponseOutbound;
    @Value("${mqtt.topic.receiveSosRetransmitCommandMessage}")
    private String receiveSosRetransmitCommandMessageTopic;

    // @Autowired
    // private MessageHandler receiveSosRetransmitCommandMessageOutbound;
    @Value("${mqtt.topic.receiveRsnResponse}")
    private String receiveRsnResponseTopic;

    // @Autowired
    // private MessageHandler receiveRsnResponseOutbound;
    @Value("${mqtt.topic.receiveRsnVctResponse}")
    private String receiveRsnVctResponseTopic;

    // @Autowired
    // private MessageHandler receiveRsnVctResponseOutbound;
    @Value("${mqtt.topic.receiveRsnRetransmitCommandMessage}")
    private String receiveRsnRetransmitCommandMessageTopic;

    // @Autowired
    // private MessageHandler receiveRsnRetransmitCommandMessageOutbound;
    @Value("${mqtt.topic.receiveVlsCommandMessage}")
    private String receiveVlsCommandMessageTopic;

    // @Autowired
    // private MessageHandler receiveVlsCommandMessageOutbound;
    @Value("${mqtt.topic.receiveVlsVehicleReportResponse}")
    private String receiveVlsVehicleReportResponseTopic;

    // @Autowired
    // private MessageHandler receiveVlsVehicleReportResponseOutbound;
    @Value("${mqtt.topic.receiveTrackingVctResponse}")
    private String receiveTrackingVctResponseTopic;

    // @Autowired
    // private MessageHandler receiveTrackingVctResponseOutbound;
    @Value("${mqtt.topic.receiveProvisioningCommandMessage}")
    private String receiveProvisioningCommandMessageTopic;

    // @Autowired
    // private MessageHandler receiveProvisioningCommandMessageOutbound;
    @Value("${mqtt.topic.receiveCustProvisioningConfiguration}")
    private String receiveCustProvisioningConfigurationTopic;

    // @Autowired
    // private MessageHandler receiveCustProvisioningConfigurationOutbound;
    @Value("${mqtt.topic.receiveDhcResponse}")
    private String receiveDhcResponseTopic;

    // @Autowired
    // private MessageHandler receiveDhcResponseOutbound;
    @Value("${mqtt.topic.receiveDhcCommandMessage}")
    private String receiveDhcCommandMessageTopic;

    // @Autowired
    // private MessageHandler receiveDhcCommandMessageOutbound;
    @Value("${mqtt.topic.receiveVoiceKillCommandMessage}")
    private String receiveVoiceKillCommandMessageTopic;

    // @Autowired
    // private MessageHandler receiveVoiceKillCommandMessageOutbound;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private MqttMessagePublisher mqttMessagePublisher;

    @Value("${mqtt.operationTraits.acnResult.messageExpiryInterval}")
    private Long acnResultMessageExpiryInterval;

    // ACN Response 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    public String receiveAcnResponse(AcnResponsePayload acnResponsePayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveAcnResponseTopic, deviceId, ecuId, appId);
            log.info("publishing the response to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(acnResponsePayload));

            String responseJson = objectMapper.writeValueAsString(acnResponsePayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, acnResultMessageExpiryInterval);

            log.info("Published the response to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the response", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // ACN VCT Response 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.acnVctResult.messageExpiryInterval}")
    private Long acnVctResultMessageExpiryInterval;

    public String receiveAcnVctResponse(AcnVctResponsePayload acnVctResponsePayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveAcnVctResponseTopic, deviceId, ecuId, appId);
            log.info("publishing the response to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(acnVctResponsePayload));

            String responseJson = objectMapper.writeValueAsString(acnVctResponsePayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, acnVctResultMessageExpiryInterval);

            log.info("Published the response to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the response", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // ACN Retransmit Command 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.acnCommand.messageExpiryInterval}")
    private Long acnCommandMessageExpiryInterval;

    public String receiveAcnRetransmitCommandMessage(RetransmitPayload acnRetransmitCommandPayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveAcnRetransmitCommandMessageTopic, deviceId, ecuId, appId);
            log.info("publishing the command to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(acnRetransmitCommandPayload));

            String responseJson = objectMapper.writeValueAsString(acnRetransmitCommandPayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, acnCommandMessageExpiryInterval);

            log.info("Published the command to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the command", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // SOS Response 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.sosResult.messageExpiryInterval}")
    private Long sosResultMessageExpiryInterval;

    public String receiveSosResponse(SosResponsePayload sosResponsePayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveSosResponseTopic, deviceId, ecuId, appId);
            log.info("publishing the response to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sosResponsePayload));

            String responseJson = objectMapper.writeValueAsString(sosResponsePayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, sosResultMessageExpiryInterval);

            log.info("Published the response to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the response", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // SOS VCT Response 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.sosVctResult.messageExpiryInterval}")
    private Long sosVctResultMessageExpiryInterval;

    public String receiveSosVctResponse(SosVctResponsePayload sosVctResponsePayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveSosVctResponseTopic, deviceId, ecuId, appId);
            log.info("publishing the response to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sosVctResponsePayload));

            String responseJson = objectMapper.writeValueAsString(sosVctResponsePayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, sosVctResultMessageExpiryInterval);

            log.info("Published the response to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the response", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // SOS Retransmit Command 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.sosCommand.messageExpiryInterval}")
    private Long sosCommandMessageExpiryInterval;

    public String receiveSosRetransmitCommandMessage(RetransmitPayload sosRetransmitCommandPayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveSosRetransmitCommandMessageTopic, deviceId, ecuId, appId);
            log.info("publishing the command to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sosRetransmitCommandPayload));

            String responseJson = objectMapper.writeValueAsString(sosRetransmitCommandPayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, sosCommandMessageExpiryInterval);

            log.info("Published the command to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the command", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // RSN Response 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.rsnReuslt.messageExpiryInterval}")
    private Long rsnReusltMessageExpiryInterval;

    public String receiveRsnResponse(RsnResponsePayload rsnResponsePayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveRsnResponseTopic, deviceId, ecuId, appId);
            log.info("publishing the response to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rsnResponsePayload));

            String responseJson = objectMapper.writeValueAsString(rsnResponsePayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, rsnReusltMessageExpiryInterval);

            log.info("Published the response to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the response", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // RSN VCT Response 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.rsnVctResult.messageExpiryInterval}")
    private Long rsnVctResultMessageExpiryInterval;

    public String receiveRsnVctResponse(RsnVctResponsePayload rsnVctResponsePayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveRsnVctResponseTopic, deviceId, ecuId, appId);
            log.info("publishing the response to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rsnVctResponsePayload));

            String responseJson = objectMapper.writeValueAsString(rsnVctResponsePayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, rsnVctResultMessageExpiryInterval);

            log.info("Published the response to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the response", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // RSN Retransmit Command 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.rsnCommand.messageExpiryInterval}")
    private Long rsnCommandMessageExpiryInterval;

    public String receiveRsnRetransmitCommandMessage(RetransmitPayload rsnRetransmitCommandPayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveRsnRetransmitCommandMessageTopic, deviceId, ecuId, appId);
            log.info("publishing the command to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rsnRetransmitCommandPayload));

            String responseJson = objectMapper.writeValueAsString(rsnRetransmitCommandPayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, rsnCommandMessageExpiryInterval);

            log.info("Published the command to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the command", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // VLS Start Command 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.vlsCommand.messageExpiryInterval}")
    private Long vlsCommandMessageExpiryInterval;

    public String receiveVlsStartCommandMessage(VlsStartPayload vlsStartCommandPayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveVlsCommandMessageTopic, deviceId, ecuId, appId);
            log.info("publishing the command to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(vlsStartCommandPayload));

            String responseJson = objectMapper.writeValueAsString(vlsStartCommandPayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, vlsCommandMessageExpiryInterval);

            log.info("Published the command to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the command", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // VLS Stop Command 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    public String receiveVlsStopCommandMessage(VlsStopPayload vlsStopCommandPayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveVlsCommandMessageTopic, deviceId, ecuId, appId);
            log.info("publishing the command to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(vlsStopCommandPayload));

            String responseJson = objectMapper.writeValueAsString(vlsStopCommandPayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, vlsCommandMessageExpiryInterval);

            log.info("Published the command to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the command", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // VLS Voice Call Command 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    public String receiveVlsVoiceCallCommandMessage(VlsVoiceCallPayload vlsVoiceCallCommandPayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveVlsCommandMessageTopic, deviceId, ecuId, appId);
            log.info("publishing the command to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(vlsVoiceCallCommandPayload));

            String responseJson = objectMapper.writeValueAsString(vlsVoiceCallCommandPayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, vlsCommandMessageExpiryInterval);

            log.info("Published the command to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the command", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // VLS Vehicle Report Response 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.vlsResult.messageExpiryInterval}")
    private Long vlsResultMessageExpiryInterval;

    public String receiveVlsVehicleReportResponse(VlsResponsePayload vlsResponsePayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveVlsVehicleReportResponseTopic, deviceId, ecuId, appId);
            log.info("publishing the response to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(vlsResponsePayload));

            String responseJson = objectMapper.writeValueAsString(vlsResponsePayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, vlsResultMessageExpiryInterval);

            log.info("Published the response to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the response", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // VLS VCT Response 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.vlsVctResult.messageExpiryInterval}")
    private Long vlsVctResultMessageExpiryInterval;

    public String receiveTrackingVctResponse(VlsVctResponsePayload vlsVctResponsePayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveTrackingVctResponseTopic, deviceId, ecuId, appId);
            log.info("publishing the response to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(vlsVctResponsePayload));

            String responseJson = objectMapper.writeValueAsString(vlsVctResponsePayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, vlsVctResultMessageExpiryInterval);

            log.info("Published the response to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the response", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // Provisioning Command 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.provisioning.messageExpiryInterval}")
    private Long provisioningMessageExpiryInterval;

    public String receiveProvisioningCommandMessage(ProvisioningPayload provisioningCommandPayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveProvisioningCommandMessageTopic, deviceId, ecuId, appId);
            log.info("publishing the command to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(provisioningCommandPayload));

            String responseJson = objectMapper.writeValueAsString(provisioningCommandPayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, provisioningMessageExpiryInterval);

            log.info("Published the command to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the command", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // CUST Provisioning Command 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.custResult.messageExpiryInterval}")
    private Long custResultMessageExpiryInterval;

    public String receiveCustProvisioningConfiguration(CustProvisioningPayload custProvisioningConfigurationPayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveCustProvisioningConfigurationTopic, deviceId, ecuId, appId);
            log.info("publishing the command to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(custProvisioningConfigurationPayload));

            String responseJson = objectMapper.writeValueAsString(custProvisioningConfigurationPayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, custResultMessageExpiryInterval);

            log.info("Published the command to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the command", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // DHC Response 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.dhcResult.messageExpiryInterval}")
    private Long dhcResultMessageExpiryInterval;

    public String receiveDhcResponse(DhcResponsePayload dhcResponsePayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveDhcResponseTopic, deviceId, ecuId, appId);
            log.info("publishing the response to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dhcResponsePayload));

            String responseJson = objectMapper.writeValueAsString(dhcResponsePayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, dhcResultMessageExpiryInterval);

            log.info("Published the response to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the response", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // DHC Command 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.dhcCommand.messageExpiryInterval}")
    private Long dhcCommandMessageExpiryInterval;

    public String receiveDhcCommandMessage(DhcCommandPayload dhcCommandPayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveDhcCommandMessageTopic, deviceId, ecuId, appId);
            log.info("publishing the command to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dhcCommandPayload));

            String responseJson = objectMapper.writeValueAsString(dhcCommandPayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, dhcCommandMessageExpiryInterval);

            log.info("Published the command to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the command", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // Voice Kill Command 송신 to Broker
    // ------------------------------------------------------------------------------------------------ //
    @Value("${mqtt.operationTraits.voiceKillCommand.messageExpiryInterval}")
    private Long voiceKillCommandMessageExpiryInterval;

    public String receiveVoiceKillCommandMessage(VoicekillPayload voicekillCommandPayload, String deviceId, String ecuId, String appId) {

        String responseTopic = "";

        if (deviceId.isEmpty() && ecuId.isEmpty() && appId.isEmpty()) {
            return "";
        }

        try {
            responseTopic = extractResponseTopic(receiveVoiceKillCommandMessageTopic, deviceId, ecuId, appId);
            log.info("publishing the command to topic: {} {}", responseTopic, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(voicekillCommandPayload));

            String responseJson = objectMapper.writeValueAsString(voicekillCommandPayload);
            mqttMessagePublisher.publishMessage(responseTopic, responseJson, 1, false, voiceKillCommandMessageExpiryInterval);

            log.info("Published the command to topic: {}", responseTopic);
        }
        catch (Exception e) {
            log.error("Failed to publish the command", e);
        }

        return responseTopic;
    }
    // ------------------------------------------------------------------------------------------------ //


    // Response Topic 추출
    // ------------------------------------------------------------------------------------------------ //
    private String extractResponseTopic(String topicPattern, String deviceId, String ecuId, String appId) {

        String compiledTopic = "";

        Map<String, String> parameterMap = new HashMap<>();

        parameterMap.put("deviceId", deviceId);
        parameterMap.put("ecuId", ecuId);
        parameterMap.put("appId", appId);

        if (parameterMap != null) {

            compiledTopic = topicPattern;

            for (String key : parameterMap.keySet()) {
                compiledTopic = compiledTopic.replace("{" + key + "}", parameterMap.get(key));
            }
        }

        return compiledTopic;
    }
    // ------------------------------------------------------------------------------------------------ //
}