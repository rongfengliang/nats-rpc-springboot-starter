package com.dalong.reactorclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.Retry;
import io.nats.client.Connection;

import java.time.Duration;

public class RpcServiceRetryReactorClient {

    private final Connection connection;
    private final ObjectMapper objectMapper;
    private final Duration timeout;
    private final Retry retry;

    public RpcServiceRetryReactorClient(Connection connection, ObjectMapper objectMapper, Duration timeout, Retry retry) {
        this.connection = connection;
        this.objectMapper = objectMapper;
        this.timeout = timeout;
        this.retry = retry;
    }

    public <T> T target(Class<T> serviceInterface) {
        return RpcServiceRetryReactorProxy.create(serviceInterface, connection, objectMapper, timeout, retry);
    }
}

