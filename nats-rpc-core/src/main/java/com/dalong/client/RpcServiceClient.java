package com.dalong.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import lombok.Builder;

import java.time.Duration;

@Builder
public class RpcServiceClient {

     private Connection connection;
     private ObjectMapper objectMapper;
     private Duration timeout;
     public <T> T target(Class<T> serviceInterface) {
           return  RpcServiceProxy.create(serviceInterface,connection, objectMapper,timeout);
     }
     public <T> T targetMsg(Class<T> serviceInterface) {
          return  RpcServiceProxy.createMsg(serviceInterface,connection, objectMapper);
     }
}
