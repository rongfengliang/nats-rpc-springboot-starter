package com.dalong.autoconfigure.bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dalong.client.RpcServiceClient;
import io.nats.client.Connection;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;

public class NatsRpcClientFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> rpcInterface;
    private final Connection connection;
    private final ObjectMapper objectMapper;

    public NatsRpcClientFactoryBean(Class<T> rpcInterface, Connection connection, ObjectMapper objectMapper) {
        this.rpcInterface = rpcInterface;
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    @Nullable
    @Override
    public T getObject() throws Exception {
        T serviceClient = RpcServiceClient.builder().objectMapper(objectMapper)
                .connection(connection)
                .build().target(rpcInterface);
        return serviceClient;
    }

    @Nullable
    @Override
    public Class<?> getObjectType() {
        return rpcInterface;
    }
}
