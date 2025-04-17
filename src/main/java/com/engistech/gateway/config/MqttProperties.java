package com.engistech.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {
    private Map<String, String> subscribe;
    private Map<String, String> publish;

    private BrokerProperties broker;
    
    public static class BrokerProperties {
        private int qos;
        // 다른 broker 관련 속성들 추가
        private String address;
        private String username;
        private String password;
        private String clientId;

        // Getter와 Setter
        public int getQos() {
            return qos;
        }

        public void setQos(int qos) {
            this.qos = qos;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    }

    public Map<String, String> getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(Map<String, String> subscribe) {
        this.subscribe = subscribe;
    }

    public Map<String, String> getPublish() {
        return publish;
    }

    public void setPublish(Map<String, String> publish) {
        this.publish = publish;
    }

    public BrokerProperties getBroker() {
        return broker;
    }

    public void setBroker(BrokerProperties broker) {
        this.broker = broker;
    }
}
