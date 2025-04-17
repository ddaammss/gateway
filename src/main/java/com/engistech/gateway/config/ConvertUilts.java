package com.engistech.gateway.config;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * packageName    : com.engistech.gateway.config
 * fileName       : ConvertUilts
 * author         : jjj
 * date           : 2025-01-24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-01-24        jjj       최초 생성
 */

@Slf4j
public class ConvertUilts {


    /**
     * MAS 단위를 위도로 변환 (소수점 13자리)
     * @param masLatitude MAS 단위로 표현된 위도
     * @return 변환된 위도 (소수점 13자리 문자열)
     */
    public static String convertMasToLatitude(String masLatitude) {
        try {
            // 문자열을 BigDecimal로 변환 후 1/3600000 적용
            BigDecimal latitude = new BigDecimal(masLatitude).divide(BigDecimal.valueOf(3600000), 13, RoundingMode.HALF_UP).stripTrailingZeros();
            return latitude.toPlainString();
        } catch (NumberFormatException e) {
            return masLatitude;
        }
    }

    /**
     * MAS 단위를 경도로 변환 (소수점 12자리)
     * @param masLongitude MAS 단위로 표현된 경도
     * @return 변환된 경도 (소수점 12자리 문자열)
     */
    public static String convertMasToLongitude(String masLongitude) {
        try {
            // 문자열을 BigDecimal로 변환 후 1/3600000 적용
            BigDecimal longitude = new BigDecimal(masLongitude).divide(BigDecimal.valueOf(3600000), 12, RoundingMode.HALF_UP).stripTrailingZeros();
            return longitude.toPlainString();
        } catch(NumberFormatException e) {
            return masLongitude;
        }
    }
}
