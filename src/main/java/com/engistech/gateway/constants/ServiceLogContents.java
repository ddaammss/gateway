package com.engistech.gateway.constants;

public final class ServiceLogContents {

    private ServiceLogContents() {
    }

    // 공통
    public static final String NOTIFICATION_SEND_DCM_TO_MQTT = "Notification 전송 (DCM -> MQTT)";
    public static final String NOTIFICATION_ACK_MQTT_TO_DCM = "Notification 전송 ACK (MQTT -> DCM)";
    public static final String VOICE_CALL_TERMINATOR_DCM_TO_MQTT = "VOICE CALL TERMINATOR (DCM -> MQTT)";
    public static final String VOICE_CALL_TERMINATOR_RESPONSE_MQTT_TO_DCM = "VOICE CALL TERMINATOR 응답 (MQTT -> DCM)";
    public static final String RETRANSMIT_ERROR_SEND_DCM_TO_MQTT = "RETRANSMIT 에러 전송 (DCM -> MQTT)";

    // SOS 메시지 구분
    public static final String SOS_DATA_SEND_MQTT_TO_CTI = "SOS 데이터 전송 (MQTT -> 중계서버)";
    public static final String SOS_RETRANSMIT_REQUEST = "SOS RETRANSMIT 요청";
    public static final String SOS_RETRANSMIT_REQUEST_MQTT_TO_DCM = "SOS RETRANSMIT 요청 전송 (MQTT -> DCM)";

    // ACN 메시지 구분
    public static final String ACN_DATA_SEND_MQTT_TO_CTI = "ACN 데이터 전송 (MQTT -> 중계서버)";
    public static final String ACN_RETRANSMIT_REQUEST = "ACN RETRANSMIT 요청";
    public static final String ACN_RETRANSMIT_REQUEST_MQTT_TO_DCM = "ACN RETRANSMIT 요청 전송 (MQTT -> DCM)";

    // RSN 메시지 구분
    public static final String RSN_DATA_SEND_MQTT_TO_CTI = "RSN 데이터 전송 (MQTT -> 중계서버)";
    public static final String RSN_RETRANSMIT_REQUEST = "RSN RETRANSMIT 요청";
    public static final String RSN_RETRANSMIT_REQUEST_MQTT_TO_DCM = "RSN RETRANSMIT 요청 전송 (MQTT -> DCM)";

    // VLS START 관련 메시지
    public static final String VLS_START_REQUEST_CCW_TO_MQTT = "VLS START 요청 (CCW -> MQTT I/F)";
    public static final String VLS_START_REQUEST_SEND_MQTT_TO_DCM = "VLS START 요청 전송 (MQTT -> DCM)";

    // VLS STOP 관련 메시지
    public static final String VLS_STOP_REQUEST_CCW_TO_MQTT = "VLS STOP 요청 (CCW -> MQTT I/F)";
    public static final String VLS_STOP_REQUEST_SEND_MQTT_TO_DCM = "VLS STOP 요청 전송 (MQTT -> DCM)";

    // VLS REPORT 관련 메시지
    public static final String VLS_REPORT_REQUEST_DCM_TO_MQTT = "VLS REPORT 요청 (DCM -> MQTT)";
    public static final String VLS_REPORT_RESPONSE_MQTT_TO_DCM = "VLS REPORT 응답 (MQTT -> DCM)";

    // VLS VOICECALL 관련 메시지
    public static final String VLS_VOICECALL_REQUEST_CCW_TO_MQTT = "VLS VOICECALL 요청 (CCW -> MQTT I/F)";
    public static final String VLS_VOICECALL_REQUEST_SEND_MQTT_TO_DCM = "VLS VOICECALL 요청 전송 (MQTT -> DCM)";

    // VOICEKILL 관련 메시지
    public static final String VOICEKILL_REQUEST_MQTT_TO_DCM = "VOICE KILL 요청 (MQTT -> DCM)";
    public static final String VOICEKILL_RESPONSE_DCM_TO_MQTT = "VOICE KILL 응답 (DCM -> MQTT)";

    // DHC Report 관련 메시지
    public static final String DHC_REPORT_SEND_DCM_TO_MQTT = "DHC Report 전송 (DCM -> MQTT)";
    public static final String DHC_REPORT_RESPONSE_MQTT_TO_DCM = "DHC Report 응답 (MQTT -> DCM)";

    // DHC Command 관련 메시지
    public static final String DHC_COMMAND_REQUEST_OPW_TO_MQTT = "DHC Command 요청 (OPW -> MQTT I/F)";
    public static final String DHC_COMMAND_REQUEST_SEND_MQTT_TO_DCM = "DHC Command 요청 전송 (MQTT -> DCM)";
    public static final String DHC_COMMAND_ERROR_SEND_MQTT_TO_DCM = "DHC Command 에러 전송 (MQTT -> DCM)";

    // PROVISIONING 관련 메시지
    public static final String PROVISIONING_REQUEST_OPW_TO_MQTT = "PROVISIONING 요청 (OPW -> MQTT I/F)";
    public static final String PROVISIONING_REQUEST_OBG_TO_MQTT = "PROVISIONING 요청 (OBG -> MQTT I/F)";
    public static final String PROVISIONING_SEND_MQTT_TO_DCM = "PROVISIONING 전송 (MQTT -> DCM)";
    public static final String PROVISIONING_RESULT_SEND_DCM_TO_MQTT = "PROVISIONING 결과 전송 (DCM -> MQTT)";

    // CUST PROVISIONING 관련 메시지
    public static final String CUST_PROVISIONING_REQUEST_DCM_TO_MQTT = "CUST PROVISIONING 요청 (DCM -> MQTT)";
    public static final String CUST_PROVISIONING_REQUEST_SEND_MQTT_TO_DCM = "CUST PROVISIONING 요청 전송 (MQTT -> DCM)";
    public static final String CUST_ACTIVATION_RESULT_SEND_DCM_TO_MQTT = "CUST 활성화 결과 (DCM -> MQTT)";

    // Simulator 요청
    public static final String COMMAND_REQUEST_SIM_TO_MQTT = "Simulator Command 요청 (SIM -> MQTT I/F)";
}
