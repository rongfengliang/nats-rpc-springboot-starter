package com.dalong.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.Retry;
import io.nats.client.Connection;

import java.lang.reflect.Proxy;
import java.time.Duration;

public class RpcServiceRetryProxy {

    public static <T> T create(Class<T> serviceInterface, Connection nats, ObjectMapper objectMapper, Duration timeout, Retry retry) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                new NatsRetryInvocationHandler(nats, objectMapper, timeout, retry)
        );
    }
    public static <T> T createGateway(Class<T> serviceInterface, Connection nats, ObjectMapper objectMapper, Duration timeout, Retry retry) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                new NatsGateWayRetryInvocationHandler(nats, objectMapper, timeout, retry)
        );
    }
}

