package com.dalong.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;

import java.lang.reflect.Proxy;
import java.time.Duration;

public class RpcServiceProxy {
    public static <T> T create(Class<T> serviceInterface, Connection nats, ObjectMapper objectMapper, Duration timeout) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                new NatsInvocationHandler(nats, objectMapper, timeout)
        );
    }
    public static <T> T createMsg(Class<T> serviceInterface, Connection nats, ObjectMapper objectMapper) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                new NatsMsgInvocationHandler(nats, objectMapper)
        );
    }
}
