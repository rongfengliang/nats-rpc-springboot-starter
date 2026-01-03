package com.dalong.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dalong.models.BaseMessage;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
public class NatsRpcClient {

    private Connection connection;
    private ObjectMapper objectMapper;
    private Duration defaultTimeout = Duration.ofSeconds(120);

    public NatsRpcClient(Connection connection, ObjectMapper objectMapper) {
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    public NatsRpcClient(Connection connection, ObjectMapper objectMapper, Duration timeout) {
        this.connection = connection;
        this.objectMapper = objectMapper;
        if (defaultTimeout != null) {
            this.defaultTimeout = timeout;
        }
    }

    public <T extends BaseMessage, R> R invoke(
            String serviceEndpoint, T request, Class<R> responseType, Duration timeout) {
        try {
            // 序列化请求消息
            String json = objectMapper.writeValueAsString(request);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            // 发送请求
            if (timeout == null) {
                timeout = defaultTimeout;
            }
            Message response = this.connection.request(serviceEndpoint, data, timeout);
            if (response != null) {
                R responseMessage = objectMapper.readValue(response.getData(), responseType);
                return responseMessage;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to request rpc service  with  service name: {}", serviceEndpoint, e);
            throw new RuntimeException("Failed to request rpc service", e);
        }
    }

    public <T extends BaseMessage, R> R invoke(
            String serviceEndpoint, T request, Headers headers, Class<R> responseType, Duration timeout) {
        try {
            // 序列化请求消息
            String json = objectMapper.writeValueAsString(request);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            // 发送请求
            if (timeout == null) {
                timeout = defaultTimeout;
            }
            Message response = this.connection.request(serviceEndpoint, headers, data, timeout);
            if (response != null) {
                R responseMessage = objectMapper.readValue(response.getData(), responseType);
                return responseMessage;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to request rpc service  with  service name: {}", serviceEndpoint, e);
            throw new RuntimeException("Failed to request rpc service", e);
        }
    }

    public <T extends BaseMessage> void sendMsg(String subjectName, T request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            this.connection.publish(subjectName, data);
        } catch (Exception e) {
            log.error("Failed to send msg to  subject  with  subject name: {}", subjectName, e);
            throw new RuntimeException("Failed to send msg to subject", e);
        }
    }

    public <T extends BaseMessage> void sendMsg(String subjectName, T request, Headers headers) {
        try {
            String json = objectMapper.writeValueAsString(request);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            this.connection.publish(subjectName, headers, data);
        } catch (Exception e) {
            log.error("Failed to send msg to  subject  with  subject name: {}", subjectName, e);
            throw new RuntimeException("Failed to send msg to subject", e);
        }
    }

    public <T extends BaseMessage, R> R invoke(
            String serviceEndpoint, T request, Headers headers, Class<R> responseType) {
        try {
            // 序列化请求消息
            String json = objectMapper.writeValueAsString(request);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            // 发送请求
            Message response = this.connection.request(serviceEndpoint, headers, data, defaultTimeout);
            if (response != null) {
                R responseMessage = objectMapper.readValue(response.getData(), responseType);
                return responseMessage;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to request rpc service  with  service name: {}", serviceEndpoint, e);
            throw new RuntimeException("Failed to request rpc service", e);
        }
    }

    public <T extends BaseMessage, R> R invoke(
            String serviceEndpoint, T request, Class<R> responseType) {
        try {
            // 序列化请求消息
            String json = objectMapper.writeValueAsString(request);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            // 发送请求
            Message response = this.connection.request(serviceEndpoint, data, defaultTimeout);
            if (response != null) {
                R responseMessage = objectMapper.readValue(response.getData(), responseType);
                return responseMessage;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to request rpc service  with  service name: {}", serviceEndpoint, e);
            throw new RuntimeException("Failed to request rpc service", e);
        }
    }

    public <T extends BaseMessage, R> R invoke(
            String serviceEndpoint, T request, TypeReference<R> responseType, Duration timeout) {
        try {
            // 序列化请求消息
            String json = objectMapper.writeValueAsString(request);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            // 发送请求
            if (timeout == null) {
                timeout = defaultTimeout;
            }
            Message response = this.connection.request(serviceEndpoint, data, timeout);
            if (response != null) {
                R responseMessage = objectMapper.readValue(response.getData(), responseType);
                return responseMessage;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to request rpc service  with  service name: {}", serviceEndpoint, e);
            throw new RuntimeException("Failed to request rpc service", e);
        }
    }

    public <T extends BaseMessage, R> R invoke(
            String serviceEndpoint, T request, Headers headers, TypeReference<R> responseType, Duration timeout) {
        try {
            // 序列化请求消息
            String json = objectMapper.writeValueAsString(request);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            // 发送请求
            if (timeout == null) {
                timeout = defaultTimeout;
            }
            Message response = this.connection.request(serviceEndpoint, headers, data, timeout);
            if (response != null) {
                R responseMessage = objectMapper.readValue(response.getData(), responseType);
                return responseMessage;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to request rpc service  with  service name: {}", serviceEndpoint, e);
            throw new RuntimeException("Failed to request rpc service", e);
        }
    }

    public <T extends BaseMessage, R> R invoke(
            String serviceEndpoint, T request, TypeReference<R> responseType) {
        try {
            // 序列化请求消息
            String json = objectMapper.writeValueAsString(request);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            // 发送请求
            Message response = this.connection.request(serviceEndpoint, data, defaultTimeout);
            if (response != null) {
                R responseMessage = objectMapper.readValue(response.getData(), responseType);
                return responseMessage;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to request rpc service  with  service name: {}", serviceEndpoint, e);
            throw new RuntimeException("Failed to request rpc service", e);
        }
    }

    public <T extends BaseMessage, R> R invoke(
            String serviceEndpoint, T request, Headers headers, TypeReference<R> responseType) {
        try {
            // 序列化请求消息
            String json = objectMapper.writeValueAsString(request);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            // 发送请求
            Message response = this.connection.request(serviceEndpoint, headers, data, defaultTimeout);
            if (response != null) {
                R responseMessage = objectMapper.readValue(response.getData(), responseType);
                return responseMessage;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to request rpc service  with  service name: {}", serviceEndpoint, e);
            throw new RuntimeException("Failed to request rpc service", e);
        }
    }

}
