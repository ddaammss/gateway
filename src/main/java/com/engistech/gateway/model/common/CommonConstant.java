package com.engistech.gateway.model.common;

/**
 * packageName    : com.engistech.gateway.model.common
 * fileName       : CommonConstant
 * author         : jjj
 * date           : 2025-01-10
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-01-10        jjj       최초 생성
 */
public class CommonConstant {
    /**
     * 메시지 구분
     * 0: REQUEST
     * 1: REPORT
     * 2: ACK
     * 3: NACK
     * 4: FAILED
     */
    public enum MESSAGE_TYPE {
         REQUEST(0)
        ,REPORT(1)
        ,ACK(2)
        ,NACK(3)
        ,FAILED(4);

        private final int value;

        MESSAGE_TYPE(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (MESSAGE_TYPE item : MESSAGE_TYPE.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     * 서비스 구분
     * 0: ACN
     * 1: SOS
     * 2: RSN
     * 3: VLS
     * 4: CUST
     * 5: DHC
     * 6: PROV
     * 7: ALL
     */
    public enum MESSAGE_SERVICE {
         ACN(0)
        ,SOS(1)
        ,RSN(2)
        ,VLS(3)
        ,CUST(4)
        ,DHC(5)
        ,PROV(6)
        ,ALL(7);

        private final int value;

        MESSAGE_SERVICE(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (MESSAGE_SERVICE item : MESSAGE_SERVICE.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     *  Operation 구분
     *  0: NOTIFICATION
     *  1: RETRANSMIT
     *  2: DHC
     *  3: CUST
     *  4: CONFIRM_TEMINATION
     *  5: START
     *  6: STOP
     *  7: VOICE_CALL
     *  8: PROVISIONING
     *  9: VOICE_KILL
     *  10: REPORT
     */
    public enum MESSAGE_OPERATION {
         NOTIFICATION(0)
        ,RETRANSMIT(1)
        ,DHC(2)
        ,CUST(3)
        ,CONFIRM_TERMINATION(4)
        ,START(5)
        ,STOP(6)
        ,VOICE_CALL(7)
        ,PROVISIONING(8)
        ,VOICE_KILL(9)
        ,REPORT(10);

        private final int value;

        MESSAGE_OPERATION(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (MESSAGE_OPERATION item : MESSAGE_OPERATION.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }
}
