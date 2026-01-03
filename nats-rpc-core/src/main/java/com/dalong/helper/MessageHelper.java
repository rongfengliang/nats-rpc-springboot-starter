package com.dalong.helper;

import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.impl.Headers;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MessageHelper {
    public static <T extends BaseMessage> T castBaseMessage(String payload, Class<T> clazz, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (IOException e) {
            log.error("Failed to cast message: {}", e.getMessage());
        }
        return null;
    }

    public static <T extends BaseMessage, H> Object actionMethodExecute(Method method, H h, T baseMessage, Headers headers) {
        Object result;
        try {
            result = method.invoke(h, baseMessage, headers);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static <T extends BaseMessage> T castBaseMessage(byte[] payload, Class<T> clazz, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (IOException e) {
            log.error("Failed to cast message: {}", e.getMessage());
        }
        return null;
    }
}
