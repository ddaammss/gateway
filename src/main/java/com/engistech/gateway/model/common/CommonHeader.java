package com.engistech.gateway.model.common;

import com.engistech.gateway.config.EmptyObjectSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonSerialize(using = EmptyObjectSerializer.class)     // Empty Object 제거를 위해
public class CommonHeader {
    private UserProperties userProperties;
    private DeviceInfo device;
    private MessageInfo message;
    private HuLanguageEnum huLanguage;
    private String transmissionTimestampUTC;

    public CommonHeader() {
        this.userProperties = new UserProperties();
        this.device = new DeviceInfo();
        this.message = new MessageInfo();
    }

    public enum HuLanguageEnum {
        JA_JP("ja-JP"),
        EN("en"),
        EN_US("en-US"),
        EN_GB("en-GB"),
        FR_FR("fr-FR"),
        DE_DE("de-DE"),
        IT_IT("it-IT"),
        NL_NL("nl-NL"),
        ES_ES("es-ES"),
        SV_SE("sv-SE"),
        DA_DK("da-DK"),
        NB_NO("nb-NO"),
        PT_PT("pt-PT"),
        RU_RU("ru-RU"),
        EL_GR("el-GR"),
        PL_PL("pl-PL"),
        FI_FI("fi-FI"),
        ZH_CN("zh-CN"),
        ZH_TW("zh-TW"),
        KO_KR("ko-KR"),
        UK_UA("uk-UA"),
        TR_TR("tr-TR"),
        HU_HU("hu-HU"),
        CS_CZ("cs-CZ"),
        SK_SK("sk-SK"),
        RO_RO("ro-RO"),
        AR_AE("ar-AE"),
        TH_TH("th-TH"),
        PT_BR("pt-BR"),
        ES_MX("es-MX"),
        FR_CA("fr-CA"),
        ES_US("es-US"),
        MS_MY("ms-MY"),
        ID_ID("id-ID"),
        EU_ES("eu-ES"),
        BG_BG("bg-BG"),
        NL_BE("nl-BE"),
        EN_AU("en-AU"),
        HI_IN("hi-IN"),
        VI_VN("vi-VN"),
        TL_PH("tl-PH"),
        TA("ta"),
        FA("fa"),
        HE("he"),
        HE_IL("he-IL"),
        UNKNOWN("UNKNOWN");

        private final String value;

        HuLanguageEnum(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }

        @JsonCreator
        public static HuLanguageEnum fromValue(String value) {
            for (HuLanguageEnum item : HuLanguageEnum.values()) {
                if (item.value.equalsIgnoreCase(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Invalid HuLanguage value: " + value);
        }
    }
}