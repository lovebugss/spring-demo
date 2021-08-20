package com.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * TODO json 工具类
 *
 * @author renjp
 * @date 2021/8/20 11:44
 */
@Slf4j
public class JsonUtils {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // ....
    }

    public static <T> T parser(String data, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(data, clazz);
        } catch (JsonProcessingException e) {
            log.error("json process exception, data: [{}], class: [{}]", data, clazz);
            return null;
        }
    }

    public static <T> T parser(String data, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(data, typeReference);
        } catch (JsonProcessingException e) {
            log.error("json process exception, data: [{}], class: [{}]", data, typeReference);
            return null;
        }
    }

    public static <T> T mapToObj(Map<String, Object> map, Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(map, clazz);
    }

    public static boolean isValidJson(String jsonStr) {
        return false;
    }
}
