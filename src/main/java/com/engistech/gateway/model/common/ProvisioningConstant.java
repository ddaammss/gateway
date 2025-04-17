package com.engistech.gateway.model.common;

/**
 * packageName    : com.engistech.gateway.model.common
 * fileName       : ProvisioningConstant
 * author         : jjj
 * date           : 2025-01-09
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-01-09        jjj       최초 생성
 */
public class ProvisioningConstant {

    /**
     * Service Type
     * 0: ACN
     * 1: SOS
     * 2: RSN
     * 3: VLS
     * 4: DHC
     */
    public enum SERVICE_TYPE {
         ACN(0)
        ,SOS(1)
        ,RSN(2)
        ,VLS(3)
        ,DHC(4);

        private final int value;

        SERVICE_TYPE(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (SERVICE_TYPE item : SERVICE_TYPE.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     * Service Type
     * 0: ACN
     * 1: SOS
     * 2: RSN
     * 3: VLS
     * 4: INBOUND
     */
    public enum PHONE_NUMBER_SERVICE_TYPE {
         ACN(0)
        ,SOS(1)
        ,RSN(2)
        ,VLS(3)
        ,INBOUND(4);

        private final int value;

        PHONE_NUMBER_SERVICE_TYPE(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (PHONE_NUMBER_SERVICE_TYPE item : PHONE_NUMBER_SERVICE_TYPE.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }

    /**
     * Phone Number Type
     * 0: PRIMARY
     * 1: SECONDARY
     * 2: TOYOTA_UNKNOWN
     * 3: LEXUS
     * 4: THIRD
     * 5: FOURTH
     * 6: FIFTH
     * 7: SIXTH
     * 8: SEVENTH
     * 9: EIGHTH
     * 10: NINTH
     * 11: TENTH
     */
    public enum PHONE_NUMBER_TYPE {
         PRIMARY(0)
        ,SECONDARY(1)
        ,TOYOTA_UNKNOWN(2)
        ,LEXUS(3)
        ,THIRD(4)
        ,FOURTH(5)
        ,FIFTH(6)
        ,SIXTH(7)
        ,SEVENTH(8)
        ,EIGHTH(9)
        ,NINTH(10)
        ,TENTH(11);

        private final int value;

        PHONE_NUMBER_TYPE(int value) {
            this.value = value;
        }

        public static int convertValue(String name) {
            for (PHONE_NUMBER_TYPE item : PHONE_NUMBER_TYPE.values()) {
                if (item.name().equalsIgnoreCase(name)) {
                    return item.value;
                }
            }
            throw new IllegalArgumentException("No enum constant for name: " + name);
        }
    }
}