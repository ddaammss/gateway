package com.engistech.gateway.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmptyObjectSerializer<T> extends JsonSerializer<T> {
    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        // 객체가 비어있는 경우 직렬화 대상 제외
        if (shouldSkipObject(value)) {
            return;
        }

        // 객체가 비어있지 않은 경우 직렬화 시작
        writeObject(value, gen, serializers);
    }

    private void writeObject(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        gen.writeStartObject();
        
        for (Field field : value.getClass().getDeclaredFields()) {

            field.setAccessible(true);

            try {
                Object fieldValue = field.get(value);
                
                // 단순 타입인 경우
                if (!isComplexObject(fieldValue)) {

                    // null이 아니면 그대로 출력
                    if (fieldValue != null) {
                        gen.writeObjectField(field.getName(), fieldValue);
                    }
                }
                // 객체 타입인 경우
                else {
                    // 비어 있지 않은 경우만 출력
                    if (!shouldSkipObject(fieldValue)) {
                        gen.writeObjectField(field.getName(), fieldValue);
                    }
                }
            }
            catch (IllegalAccessException e) {
                log.error("Failed to access the field '{}' in the object of type '{}'.", field.getName(), value.getClass().getName(), e);
            }
        }
        
        gen.writeEndObject();
    }

    private boolean shouldSkipObject(Object obj) {

        // 단순 타입이면서 null인 경우, skip
        if (!isComplexObject(obj)) {
            return obj == null;
        }

        // 객체인 경우, null이거나 비어있으면 skip
        return obj == null || isEmptyObject(obj);
    }

    // 사용자 정의 객체 여부 확인
    private boolean isComplexObject(Object obj) {
        if (obj == null) return false;
        return !(obj.getClass().isPrimitive() || 
                obj instanceof String || 
                obj instanceof Number || 
                obj instanceof Enum ||
                obj instanceof Boolean);
    }

    // 객체의 모든 필드가 비어있는지 확인
    private boolean isEmptyObject(Object obj) {
        return Arrays.stream(obj.getClass().getDeclaredFields())
            .peek(f -> f.setAccessible(true))
            .allMatch(f -> {
                try {
                    Object value = f.get(obj);
                    if (!isComplexObject(value)) {
                        return value == null;
                    }
                    return shouldSkipObject(value);
                } 
                catch (IllegalAccessException e) {
                    return true;
                }
            });
    }
}
