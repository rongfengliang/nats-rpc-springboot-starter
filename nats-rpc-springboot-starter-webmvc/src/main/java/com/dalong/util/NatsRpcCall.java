package com.dalong.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;

import java.time.Duration;

public class NatsRpcCall {
    public static JsonNode call(Connection connection, ObjectMapper objectMapper, String subject, byte[] requestPayload, Headers headers, long timeoutSeconds) throws Exception {
        // Implementation of the NATS RPC call goes here
        Message response = null;
        try {
            response = connection.request(subject, headers, requestPayload, Duration.ofSeconds(timeoutSeconds));
            if (response != null) {
                var responseMessage = objectMapper.readTree(response.getData());
                return responseMessage;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static byte[] serializeMessage(ObjectMapper objectMapper, Object message) {
        try {
            return objectMapper.writeValueAsBytes(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode call(Connection connection, ObjectMapper objectMapper, String subject, byte[] requestPayload, Headers headers) {
        // Implementation of the NATS RPC call goes here
        Message response = null;
        try {
            response = connection.request(subject, headers, requestPayload, Duration.ofSeconds(120));
            if (response != null) {
                var responseMessage = objectMapper.readTree(response.getData());
                return responseMessage;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void sendMsg(Connection connection, String subject, byte[] requestPayload, Headers headers) {
        try {
            connection.publish(subject, headers, requestPayload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
