package com.dalong.reactorclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import lombok.Builder;

import java.time.Duration;

@Builder
public class RpcServiceReactorClient {

    private Connection connection;
    private ObjectMapper objectMapper;
    private Duration timeout;

    public <T> T target(Class<T> serviceInterface) {
        return RpcServiceReactorProxy.create(serviceInterface, connection, objectMapper, timeout);
    }
}
