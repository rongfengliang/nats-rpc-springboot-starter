package com.dalong.client;

import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import io.nats.client.impl.Headers;

public interface NatsRpcGateWay<T extends BaseMessage> {
    <R> R gateway(@RpcMethod NatsMethod method, @RpcPayload T t, @RpcHeaders Headers headers);
    <R> R gateway(@RpcMethod NatsMethod method, @RpcPayload T t, @RpcHeaders Headers headers, Class<R> clazz);
    <R> R gateway(@RpcMethod NatsMethod method, @RpcPayload T t, @RpcHeaders Headers headers, TypeReference<R> typeRef);
}
