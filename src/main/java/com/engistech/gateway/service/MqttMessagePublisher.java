package com.engistech.gateway.service;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MqttMessagePublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttMessagePublisher.class);
    
    private final MqttAsyncClient mqttAsyncClient;
    private final MqttService mqttService;

    //@Autowired
    public MqttMessagePublisher(MqttAsyncClient mqttAsyncClient, MqttService mqttService) {
        this.mqttAsyncClient = mqttAsyncClient;
        this.mqttService = mqttService;
    }

    public void publishMessage(String topic, String payload, int qos, boolean retained, Long messageExpiryInterval) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            message.setRetained(retained);
            mqttService.insertPayload(topic, payload);

            // MQTT 5.0 속성 추가
            MqttProperties properties = new MqttProperties();
            properties.setContentType("application/json");
            properties.setPayloadFormat(true);
            properties.setMessageExpiryInterval(messageExpiryInterval);
            message.setProperties(properties);

            mqttAsyncClient.publish(topic, message, null, new MqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    LOGGER.info("Published the message to topic: {}", topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    LOGGER.error("Failed to publish the message to topic: {}", topic, exception);
                }
            });
        } catch (MqttException e) {
            LOGGER.error("Error while publishing the message to topic: {}", topic, e);
        }
    }
}
