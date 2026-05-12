package com.dalong.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.Retry;
import io.nats.client.Connection;

import java.time.Duration;

public class RpcServiceRetryClient {

    private final Connection connection;
    private final ObjectMapper objectMapper;
    private final Duration timeout;
    private final Retry retry;

    public RpcServiceRetryClient(Connection connection, ObjectMapper objectMapper, Duration timeout, Retry retry) {
        this.connection = connection;
        this.objectMapper = objectMapper;
        this.timeout = timeout;
        this.retry = retry;
    }

    public <T> T targetGateway(Class<T> serviceInterface) {
        return RpcServiceRetryProxy.createGateway(serviceInterface, connection, objectMapper, timeout, retry);
    }

    public <T> T target(Class<T> serviceInterface) {
        return RpcServiceRetryProxy.create(serviceInterface, connection, objectMapper, timeout, retry);
    }
}

