package com.engistech.gateway.config;


import com.engistech.gateway.service.MessageHandlerService;
import com.engistech.gateway.service.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringUtils;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Map;
import java.util.UUID;

import javax.annotation.processing.Generated;

import org.springframework.context.annotation.Lazy;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.security.cert.Certificate;

@Slf4j
@Generated(value="com.cmm.asyncapi.generator.template.spring", date="2024-11-01T09:28:20.730Z")
@Configuration
public class AsyncApiConfig {

    @Autowired
    @Lazy
    private MessageHandlerService messageHandlerService;

    @Autowired
    private MqttService mqttService;

    @Autowired
    private MqttProperties mqttProperties;

    private MqttAsyncClient mqttAsyncClient;

    @Value("${mqtt.broker.timeout.connection}")
    private int connectionTimeout;

    @Value("${mqtt.broker.timeout.disconnection}")
    private long disconnectionTimeout;

    @Value("${mqtt.broker.timeout.completion}")
    private long completionTimeout;

    @Value("${mqtt.broker.keep-alive-interval:60}")
    private int keepAliveInterval;

    @Value("${mqtt.broker.clientId:defaultAuto_ClientId}")
    private String clientIdProp;

    @Value("${mqtt.broker.username}")
    private String username;

    @Value("${mqtt.broker.password}")
    private String password;

    @Value("${mqtt.topic.ecuIdString}")
    private String ecuIdString;

    @Value("${mqtt.topic.appIdString}")
    private String appIdString;

    private boolean isUsingProductionServer = true;

    private String generateUniqueClientId() {
        if (isUsingProductionServer) {
            return "ltc-cc-wam-d-1";
        } else {
            return clientIdProp + "_" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    public SSLContext createSslContext(String keyFilePath, String certFilePath) throws Exception {
        // Load the private key
        PrivateKey privateKey = loadPrivateKey(keyFilePath);

        // Load the certificate
        X509Certificate clientCert = loadCertificate(certFilePath);

        // Create a KeyStore and add the private key and certificate
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null); // Initialize the KeyStore
        keyStore.setKeyEntry("client", privateKey, null, new java.security.cert.Certificate[]{clientCert});

        // Initialize KeyManagerFactory with the KeyStore
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, null);

        // Create a TrustManager that trusts the client certificate
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        // Create the SSLContext with the KeyManager and TrustManager
        // TrustManager[] trustAllCerts = new TrustManager[] {
        //     new X509TrustManager() {
        //         public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
        //         public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
        //         public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
        //     }
        // };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
        // sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        return sslContext;
    }

    private PrivateKey loadPrivateKey(String keyFilePath) throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(keyFilePath)));

        // 민감한 문자열을 나누어 변수에 저장
        String part1 = "-----BEGIN";
        String part2 = " PRIVATE";
        String part3 = " KEY-----";
        String beginMarker = part1 + part2 + part3;

        String part4 = "-----END";
        String part5 = " PRIVATE";
        String part6 = " KEY-----";
        String endMarker = part4 + part5 + part6;

        key = key.replace(beginMarker, "").replace(endMarker, "").replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private X509Certificate loadCertificate(String certFilePath) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        try (InputStream certInputStream = new FileInputStream(certFilePath)) {
            return (X509Certificate) certificateFactory.generateCertificate(certInputStream);
        }
    }

    public PrivateKey loadPrivateKeyFromFile(String keyFilePath) throws Exception {
        try (PEMParser pemParser = new PEMParser(new FileReader(keyFilePath))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (object instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo) {
                // Extract the private key
                return converter.getPrivateKey((org.bouncycastle.asn1.pkcs.PrivateKeyInfo) object);
            } else if (object instanceof org.bouncycastle.openssl.PEMKeyPair) {
                // Handle key-pair PEM
                return converter.getPrivateKey(((org.bouncycastle.openssl.PEMKeyPair) object).getPrivateKeyInfo());
            } else {
                throw new IllegalArgumentException("Invalid private key format in file: " + keyFilePath);
            }
        }
    }

    public SSLSocketFactory getSocketFactory(String rootCAFile, String intermediateCAFile, String crtFile, String keyFile, String password) throws Exception {
 
        // Load KeyStore for Client Certificate
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);  // Initialize empty keystore
 
        // Load client certificate
        try (FileInputStream crtInputStream = new FileInputStream(crtFile)) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate clientCert = (X509Certificate) certFactory.generateCertificate(crtInputStream);
            keyStore.setCertificateEntry("clientCert", clientCert);
        }
 
        // Load Private Key
        PrivateKey privateKey = loadPrivateKeyFromFile(keyFile); // 실제 키 로딩 함수 필요
        keyStore.setKeyEntry("client", privateKey, null, new Certificate[]{keyStore.getCertificate("clientCert")});
 
        // Load CA certificates into TrustStore
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
 
        if (rootCAFile != null) {
            try (FileInputStream caInputStream = new FileInputStream(rootCAFile)) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(caInputStream);
                trustStore.setCertificateEntry("rootCA", caCert);
            }
        }
 
        if (intermediateCAFile != null) {
            try (FileInputStream intermediateInputStream = new FileInputStream(intermediateCAFile)) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                X509Certificate intermediateCert = (X509Certificate) certFactory.generateCertificate(intermediateInputStream);
                trustStore.setCertificateEntry("intermediateCA", intermediateCert);
            }
        }
 
        // Initialize TrustManager
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
 
        // Initialize KeyManager
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, null);
 
        // Create SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
 
        return sslContext.getSocketFactory();
    }
/*
    public SSLSocketFactory getSocketFactory(String caCrtFile, String crtFile, String keyFile, String password) throws Exception {
        // Load client certificate
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream crtInputStream = new FileInputStream(crtFile);
             FileInputStream keyInputStream = new FileInputStream(keyFile)) {
            // Combine client certificate and private key into a PKCS12 keystore
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate clientCert = (X509Certificate) certFactory.generateCertificate(crtInputStream);

            KeyStore tempKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            tempKeyStore.load(null, null);
            tempKeyStore.setCertificateEntry("clientCert", clientCert);

            // Load private key (requires external library like BouncyCastle or OpenSSL for PEM parsing)
            // Assumes you have a utility to parse the private key and convert to Key object
            Key clientKey = loadPrivateKeyFromFile(keyFile); // Replace with actual private key loading method

            keyStore.load(null, null);
            if (password != null) {
                keyStore.setKeyEntry("client", clientKey, password.toCharArray(), new Certificate[]{clientCert});
            } else {
                keyStore.setKeyEntry("client", clientKey, null, new Certificate[]{clientCert});
            }
        }

        // Load CA certificate if provided
        if (caCrtFile != null) {
            try (FileInputStream caInputStream = new FileInputStream(caCrtFile)) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(caInputStream);

                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                trustStore.setCertificateEntry("caCert", caCert);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
            }
        }

        // Create KeyManagerFactory
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        if (password != null) {
            keyManagerFactory.init(keyStore, password.toCharArray());
        } else {
            keyManagerFactory.init(keyStore, null);
        }

        // Create SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        return sslContext.getSocketFactory();
    }
*/
    public MqttConnectionOptions mqttConnectOptions() throws Exception {
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setAutomaticReconnect(true);
        options.setCleanStart(true);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);

        String targetURI = System.getenv("PRODUCTIONURL");
        if ((targetURI == null) || targetURI.isEmpty())
        {
            targetURI = mqttProperties.getBroker().getAddress();
            isUsingProductionServer = false;
        }
        log.info("targetURI: {}", targetURI);

        options.setServerURIs(new String[]{targetURI});
        if (targetURI.startsWith("ssl://"))
        {
            // SSLContext sslContext = createSslContext("/app/certs/pre-api01-m-client-no-pass.key", "/app/certs/pre-api01-m-client.crt");
            // SSLContext sslContext = createSslContext("/app/certs/api01-m.niyonpp.krenv1.tconsv.com.client.key", "/app/certs/api01-m.niyonpp.krenv1.tconsv.com.client.crt");
            // SSLContext sslContext = createSslContext("/app/certs/api01-m.niyonpp.krenv1.tconsv.com.client.key", "/app/certs/api01-m.niyonpp.krenv1.tconsv.com.client.crt");

            // // get password
            // String keystorePassword = getKeystorePassword();

            // // load KeyStore
            // KeyStore keyStore = KeyStore.getInstance("PKCS12");
            // try (FileInputStream keyStoreInput = new FileInputStream("/app/certs/pre-api01-m-client.pfx")) {
            //     keyStore.load(keyStoreInput, keystorePassword.toCharArray());
            // }

            // // Init KeyManagerFactory
            // KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            // keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

            // KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // trustStore.load(null, null);
            // CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            // X509Certificate certificate = (X509Certificate)certificateFactory.generateCertificate(new FileInputStream("/app/certs/pre-api01-m-client.crt"));
            // trustStore.setCertificateEntry("ca", certificate);

            // // Init TrustManagerFactory
            // TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // trustManagerFactory.init(trustStore);

            // // Init SSLContext
            // SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            // // sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            // sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            // Setting SSLContext
            //options.setSocketFactory(getSocketFactory(null, "/app/certs/api01-m.niyonpp.krenv1.tconsv.com.client.crt", "/app/certs/api01-m.niyonpp.krenv1.tconsv.com.client.key", null));
            options.setSocketFactory(getSocketFactory("/app/certs/RootCA.pem", "/app/certs/IntermediateCA.pem", "/app/certs/TMKR_24DCM.pem", "/app/certs/private.key", null));
        } else {
            if (StringUtils.hasText(username)) {
                options.setUserName(username);
            }
            if (StringUtils.hasText(password)) {
                options.setPassword(new String(password).getBytes());
            }
        }
        return options;
    }

    @Bean
    public MqttAsyncClient mqttClient() throws MqttException {

        try {
            MqttConnectionOptions options = mqttConnectOptions(); // 기존 MqttConnectionOptions 메서드 사용
            String brokerUrl = options.getServerURIs()[0];
            String clientId = generateUniqueClientId(); // 클라이언트 ID 생성 메서드 사용

            // MqttAsyncClient 인스턴스 생성
//             mqttAsyncClient = new MqttAsyncClient(brokerUrl, clientId, new MemoryPersistence());
            mqttAsyncClient = new MqttAsyncClient(brokerUrl, clientId);
            setupCallback();

            // MQTT 연결 설정
            tryConnect(options, 5);

            // 구독 메서드 호출
            subscribeToTopics(); // mqttAsyncClient를 사용해 구독 메서드 호출

            return mqttAsyncClient;
        }
        catch (Exception e) {
            log.error("Failed to create MQTT client", e);
            throw new RuntimeException("Failed to create MQTT client", e);
        }
    }

    private void subscribeToTopics() {
        try {
            Map<String, String> topics = getSubscribeTopics();
            for (String topic : topics.values()) {
                mqttAsyncClient.subscribe(topic, 1, null, new MqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        log.info("Successfully subscribed to topic: {}", topic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        log.error("Failed to subscribe to topic: {}", topic, exception);
                    }
                });
            }
        } catch (MqttException e) {
            log.error("Error subscribing to topics", e);
        }
    }

    private void setupCallback() {
        mqttAsyncClient.setCallback(new MqttCallback() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                log.info("Connected to MQTT v5 broker at {}", serverURI);
                if (reconnect) {
                    subscribeToTopics();
                }
            }

            @Override
            public void messageArrived(String receivedTopic, MqttMessage message) throws Exception {

                String payload = new String(message.getPayload());
                log.info("Received message on topic {}: {}", receivedTopic, payload);

                // Message<?> 객체 생성 및 처리
                Message<String> msg = MessageBuilder.withPayload(payload)
                        .setHeader(MqttHeaders.RECEIVED_TOPIC, receivedTopic)
                        .build();

                //Topic validation
                validateTopic(receivedTopic);

                try {
                    messageHandlerService.handleReceivedMessage(msg);
                    mqttService.insertPayload(receivedTopic, msg.getPayload());
                }
                catch (Exception e) {
                    log.error("Error handling message from topic {}: {}", receivedTopic, e.getMessage(), e);
                }
            }

            @Override
            public void disconnected(MqttDisconnectResponse disconnectResponse) {
                log.info("Disconnected from MQTT v5 broker: {}", disconnectResponse.getReasonString());
            }

            @Override
            public void mqttErrorOccurred(MqttException exception) {
                log.error("MQTT v5 error occurred: {}", exception.getMessage());
            }

            @Override
            public void authPacketArrived(int reasonCode, org.eclipse.paho.mqttv5.common.packet.MqttProperties properties) {
                log.info("Auth packet arrived: reasonCode = {}", reasonCode);
            }

            @Override
            public void deliveryComplete(IMqttToken token) {
                log.info("MQTT v5 message delivery complete.");
            }
        });
    }

    private void tryConnect(MqttConnectionOptions options, int retries) {
        int attempt = 0;
        while (attempt < retries) {
            try {
                if (!isConnected()) {
                    mqttAsyncClient.connect(options).waitForCompletion();
                    log.info("Successfully connected to MQTT broker at URL: {}", options.getServerURIs()[0]);
                    break;
                }
            } catch (Exception e) {
                attempt++;

                if (e instanceof MqttException) {
                    MqttException mqttException = (MqttException) e;
                    log.error("MQTT connection attempt {} failed with reason code: {}, message: {}, full stack trace:",
                            attempt, mqttException.getReasonCode(), mqttException.getMessage(), e);
                } else {
                    log.error("MQTT connection attempt {} failed: {}", attempt, e.getMessage());
                }
                if (attempt == retries) {
                    log.error("Max retries reached. Could not connect to MQTT broker.");
                }
                try {
                    Thread.sleep(2000); // Wait before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private boolean isConnected() {
        try {
            return mqttAsyncClient.isConnected();
        } catch (Exception e) {
            log.error("Error while checking MQTT client connection status: {}", e.getMessage(), e);
            return false;
        }
    }

    private Map<String, String> getSubscribeTopics() {
        Map<String, String> topics = mqttProperties.getSubscribe();
        log.info("Loaded MQTT Subscribe Topics: {}", topics);
        return topics;
    }

    private String getKeystorePassword() throws Exception {
        String password = System.getenv("KEYSTORE_PASSWORD");

        if ((password == null) || password.isEmpty()) {
            log.info("Keystore password not found in environment variables, loading from file.");
            password = new String(Files.readAllBytes(Paths.get("src/main/resources/config/keystore-password.txt"))).trim();
        } else {
            log.info("Keystore password successfully loaded from environment variables.");
        }

        if ((password == null) || password.isEmpty()) {
            log.warn("Keystore password is empty or not found.");
        } else {
            char firstChar = password.charAt(0);
            char lastChar = password.charAt(password.length() - 1);
            log.info("Keystore password loaded successfully. First and last characters: '{}...{}'", firstChar, lastChar);
        }

        return password;
    }

    //------------------------------------------------------------------------------------//
    // Topic의 DeviceId, EcuId, appId 유호성 검사
    // Fail 이면 Topic 무시 한다.

    private void validateTopic(String topic) throws Exception {
        List<String> parameters = new ArrayList<>();
        // Topic Parameters 추출 위치
        List<Integer> PARAMS_POSITIONS = Arrays.asList(0, 2, 3);
        String[] parts = topic.split("/");

        try {
            // deviceId, ecuId, appId 추출
            for (int position : PARAMS_POSITIONS) {
                if (position >= 0 && position < parts.length) {
                    parameters.add(parts[position]);
                } else {
                    log.info("Position " + position + " is out of bounds for topic: " + topic);
                }
            }

            // Topic 정보 갯수 확인
            if (parameters.size() < 3) {
                throw new IllegalArgumentException("Topic is not vlalid.");
            }
            //DeviceId 대소문자,숫자 및 Length (17자리) 확인
            String deviceId = parameters.get(0);
            if (!deviceId.matches("^[0-9A-Za-z]{17}$")) {
                throw new IllegalArgumentException("DeviceId is not vlalid.");
            }
            //Ecuid = DESTSW 확인
            String ecuId = parameters.get(1);
            if (!ecuId.equals(ecuIdString)) {
                throw new IllegalArgumentException("EcuId is not vlalid.");
            }
            //AppId = safety 확인
            String appId = parameters.get(2);
            if (!appId.equals(appIdString)) {
                throw new IllegalArgumentException("AppId is not vlalid.");
            }

        } catch (Exception e) {
            log.error("" + e);
            throw e;
        }
    }
    //------------------------------------------------------------------------------------//
}
