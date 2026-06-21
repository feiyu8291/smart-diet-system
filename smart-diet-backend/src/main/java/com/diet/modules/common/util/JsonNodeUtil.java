package com.diet.modules.common.util;

import com.diet.modules.common.exception.BusinessException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 统一 JSON 序列化/反序列化工具类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@UtilityClass
@Slf4j
public class JsonNodeUtil {

    private static final AtomicReference<ObjectMapper> objectMapperCache = new AtomicReference<>();
    private static final ZoneId zoneId = ZoneId.of("GMT+8");

    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapperOnce = objectMapperCache.get();
        if (objectMapperOnce != null) {
            return objectMapperOnce;
        }
        synchronized (JsonNodeUtil.class) {
            ObjectMapper objectMapperSecond = objectMapperCache.get();
            if (objectMapperSecond == null) {
                ObjectMapper objectMapperNew = new ObjectMapper();
                objectMapperNew.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                JavaTimeModule javaTimeModule = new JavaTimeModule();

                javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                javaTimeModule.addDeserializer(LocalDate.class, buildLocalDateDeserializer());

                javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                javaTimeModule.addDeserializer(LocalDateTime.class, buildLocalDateTimeDeserializer());

                objectMapperNew.registerModule(javaTimeModule);
                objectMapperNew.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                objectMapperNew.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                objectMapperCache.set(objectMapperNew);
            }
        }
        return objectMapperCache.get();
    }

    private static StdDeserializer<LocalDate> buildLocalDateDeserializer() {
        return new StdDeserializer<LocalDate>(LocalDate.class) {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getText().trim();
                try {
                    return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException e1) {
                    try {
                        long timestamp = Long.parseLong(value);
                        Instant instant = (value.length() == 10) ? Instant.ofEpochSecond(timestamp) : Instant.ofEpochMilli(timestamp);
                        return instant.atZone(zoneId).toLocalDate();
                    } catch (NumberFormatException e2) {
                        @SuppressWarnings("unchecked") Map<String, Object> obj = p.readValueAs(Map.class);
                        log.error("====>>LocalDate解析异常：【{}】，完整对象：{}", value, obj);
                        return LocalDate.of(1970, 1, 1);
                    }
                }
            }
        };
    }

    private static StdDeserializer<LocalDateTime> buildLocalDateTimeDeserializer() {
        return new StdDeserializer<LocalDateTime>(LocalDateTime.class) {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getText().trim();
                try {
                    return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (DateTimeParseException e1) {
                    try {
                        long timestamp = Long.parseLong(value);
                        if (value.length() == 10) {
                            return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), zoneId);
                        } else {
                            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId);
                        }
                    } catch (NumberFormatException e2) {
                        @SuppressWarnings("unchecked") Map<String, Object> obj = p.readValueAs(Map.class);
                        log.error("====>>LocalDateTime解析异常：【{}】，完整对象：{}", value, obj);
                        return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
                    }
                }
            }
        };
    }

    public static String writeValueAsString(Object obj) {
        try {
            return getObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw BusinessException.withMessageAndCause("请求参数JSON序列化失败", e);
        }
    }

    public static JsonNode readTree(String json) {
        try {
            return getObjectMapper().readTree(json);
        } catch (JsonProcessingException e) {
            throw BusinessException.withMessageAndCause("请求参数JSON序列化失败", e);
        }
    }

    public static <T> T readValue(String json, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw BusinessException.withMessageAndCause("读取 JSON 为对象失败", e);
        }
    }

    public static <T> T readValue(String json, TypeReference<T> typeReference) {
        try {
            return getObjectMapper().readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw BusinessException.withMessageAndCause("读取 JSON 为复杂泛型对象失败", e);
        }
    }

    public static <T> List<T> readValueList(JsonNode resultArray, Class<T> targetClazz) {
        return readValueList(resultArray.toString(), targetClazz);
    }

    public static <T> List<T> readValueList(String resultStr, Class<T> targetClazz) {
        try {
            ObjectMapper objMapper = getObjectMapper();
            return objMapper.readValue(resultStr, objMapper.getTypeFactory().constructCollectionType(List.class, targetClazz));
        } catch (Exception e) {
            log.error("【resultStr】数组转换为{}失败，数据源：{}", targetClazz.getSimpleName(), resultStr, e);
        }
        return Collections.emptyList();
    }
}
