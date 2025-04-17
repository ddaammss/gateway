package com.engistech.gateway.model.common;

/**
 * packageName    : com.engistech.gateway.model.common
 * fileName       : VehicleReportConstant
 * author         : jjj
 * date           : 2025-01-06
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-01-06        jjj       최초 생성
 */
public class VehicleReportConstant {

    /**
     * Report 종류 구분
     * 0: vehicle_report
     * 1: dhc_report
     */
    public enum REPORT_DIV {
         VEHICLE_REPORT(0)
        ,DHC_REPORT(1);

        private final int value;

        REPORT_DIV(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Vehicle ignition status indicator
     * 0: RUN
     * 1: IG_ON
     * 2: ACC_ON
     * 3: IG_OFF
     * 4: UNKNOWN
     */
    public enum IGNITION_STATE {
         RUN(0)
        ,IG_ON(1)
        ,ACC_ON(2)
        ,IG_OFF(3)
        ,UNKNOWN(4);

        private final int value;

        IGNITION_STATE(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (IGNITION_STATE item : IGNITION_STATE.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }

    }

    /**
     * Backup Battery 사용 여부
     * 0: YES
     * 1: NO
     * 2: UNKNOWN
     */
    public enum BUB_IN_USE {
         YES(0)
        ,NO(1)
        ,UNKNOWN(2);

        private final int value;

        BUB_IN_USE(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (BUB_IN_USE item : BUB_IN_USE.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     * 이벤트 트리거
     * ACN  => 0: GSW, 1: CENTER_REQUEST, 2: OTHER
     * CUST => 0: IG_ON, 1: TELEMA_ON, 2: OTHER
     * RSN => 0: HU, 1: CENTER_REQUEST, 2: OTHER
     * SOS => 0: EDSS, 1: SOS_BUTTON, 2: CENTER_REQUEST, 3: OTHER
     * VLS => 0: IG_ON, 1: IG_OFF, 2: PERIODIC, 3: CENTER_REQUEST, 4: BATTERY_DISCONNECT
     */
    public enum EVENT_TRIGGER {
         ACN_GSW(0)
        ,ACN_CENTER_REQUEST(1)
        ,ACN_OTHER(2)
        ,CUST_IG_ON(0)
        ,CUST_TELEMA_ON(1)
        ,CUST_OTHER(2)
        ,RSN_HU(0)
        ,RSN_CENTER_REQUEST(1)
        ,RSN_OTHER(2)
        ,SOS_EDSS(0)
        ,SOS_SOS_BUTTON(1)
        ,SOS_CENTER_REQUEST(2)
        ,SOS_OTHER(3)
        ,VLS_IG_ON(0)
        ,VLS_IG_OFF(1)
        ,VLS_PERIODIC(2)
        ,VLS_CENTER_REQUEST(3)
        ,VLS_BATTERY_DISCONNECT(4);

        private final int value;

        EVENT_TRIGGER(int value) {
            this.value = value;
        }

        public static int convertValue(String messageService, String name) {
            for (EVENT_TRIGGER item : EVENT_TRIGGER.values()) {
                if (item.name().equalsIgnoreCase(messageService +"_" + name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     * 상태
     * 0: ON
     * 1: OFF
     * 2: UNKNOWN
     */
    public enum STATE {
         ON(0)
        ,OFF(1)
        ,UNKNOWN(2);

        private final int value;

        STATE(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (STATE item : STATE.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     * Side Impact Sensor
     * 0: LEFT
     * 1: Right
     * 2: OFF
     * 3: UNKNOWN
     */
    public enum SIDE_IMPACT_SENSOR {
         LEFT(0)
        ,Right(1)
        ,OFF(2)
        ,UNKNOWN(3);

        private final int value;

        SIDE_IMPACT_SENSOR(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (SIDE_IMPACT_SENSOR item : SIDE_IMPACT_SENSOR.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     * Impact Type
     * 0: First Impact
     * 1: Second Impact
     */
    public enum IMPACT_TYPE {
         FIRST_IMPACT(0)
        ,SECOND_IMPACT(1);

        private final int value;

        IMPACT_TYPE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 좌석 위치 구분
     * 0: driver
     * 1: passenger
     * 2: CenterRear
     * 3: LeftRear
     * 4: RightRear
     * 5: CenterThirdRow
     * 6: LeftThirdRow
     * 7: RightThirdRow
     */
    public enum SEAT_POSITION_TYPE {
         DRIVER(0)
        ,PASSENGER(1)
        ,CENTER_REAR(2)
        ,LEFT_REAR(3)
        ,RIGHT_REAR(4)
        ,CENTER_THIRD_ROW(5)
        ,LEFT_THIRD_ROW(6)
        ,RIGHT_THIRD_ROW(7);

        private final int value;

        SEAT_POSITION_TYPE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}