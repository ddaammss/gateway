package com.engistech.gateway.model.common;

/**
 * packageName    : com.engistech.gateway.model.common
 * fileName       : VlsConstant
 * author         : jjj
 * date           : 2025-01-09
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-01-09        jjj       최초 생성
 */
public class VlsConstant {

    /**
     * Emergercy 여부
     * 0: EMERGENCY
     * 1: NON_EMERGENCY
     */
    public enum PRIORITY {
         EMERGENCY(0)
        ,NON_EMERGENCY(1);

        private final int value;

        PRIORITY(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (PRIORITY item : PRIORITY.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     * 0: ON
     * 1: OFF
     */
    public enum ON_OFF {
         ON(0)
        ,OFF(1);

        private final int value;

        ON_OFF(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (ON_OFF item : ON_OFF.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     * 발생 이벤트간 시간 간격 단위
     * 0: SEC
     * 1: MIN
     */
    public enum INTERVAL {
         SEC(0)
        ,MIN(1);

        private final int value;

        INTERVAL(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (INTERVAL item : INTERVAL.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     * Reporting 자동 종료 전 시간 단위
     * 0: SEC
     * 1: MIN
     * 2: HR
     * 3: DAYS
     */
    public enum TIME_LIMIT_UNIT {
         SEC(0)
        ,MIN(1)
        ,HR(2)
        ,DAYS(3);

        private final int value;

        TIME_LIMIT_UNIT(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (TIME_LIMIT_UNIT item : TIME_LIMIT_UNIT.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     * Call 종료 구분
     * 0: CUSTOMER
     * 1: OTHER
     */
    public enum TERMINATION_DIV {
         CUSTOMER(0)
        ,OTHER(1);

        private final int value;

        TERMINATION_DIV(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (TERMINATION_DIV item : TERMINATION_DIV.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    public enum YES_NO {
        YES(0)
        ,NO(1);

        private final int value;

        YES_NO(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (YES_NO item : YES_NO.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }
}
