package com.dalong.reactorclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;

import java.lang.reflect.Proxy;
import java.time.Duration;

public class RpcServiceReactorProxy {
    public static <T> T create(Class<T> serviceInterface, Connection nats, ObjectMapper objectMapper, Duration timeout) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                new NatsInvocationReactorHandler(nats, objectMapper, timeout)
        );
    }
}
