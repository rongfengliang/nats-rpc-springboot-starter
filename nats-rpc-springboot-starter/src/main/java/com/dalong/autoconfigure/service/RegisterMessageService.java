package com.dalong.autoconfigure.service;

import com.dalong.autoconfigure.config.RpcServiceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dalong.handler.SubMessageHandler;
import com.dalong.registry.ServiceHandlerRegistry;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RegisterMessageService {

    private Map<String, Dispatcher> dispatcherMap = new ConcurrentHashMap<>();

    public RegisterMessageService(List<SubMessageHandler> subMessageHandlers, Connection connection, RpcServiceConfig config, ServiceHandlerRegistry serviceHandlerRegistry, ObjectMapper objectMapper) {
        subMessageHandlers.forEach(subMessageHandler -> {
            String serviceName = config.getRpcServiceName();
            String servicePrefix = config.getRpcServicePrefix();
            String serviceMsgSubjectFormat = "%s.msg.%s.%s";
            Dispatcher dispatcher = connection.createDispatcher();
            String subject = String.format(serviceMsgSubjectFormat, serviceName, servicePrefix, subMessageHandler.subjectName());
            dispatcher.subscribe(subject, "messagequeue", subMessageHandler);
            subMessageHandler.registerSubTypes(objectMapper);
            dispatcherMap.put(subject, dispatcher);
            log.info("Subscribing  subject: {} with type: {}", subject, subMessageHandler.getMessageType());
        });
    }

    public void unSubscribeAll() {
        dispatcherMap.keySet().forEach(sub -> {
            Dispatcher dispatcher = dispatcherMap.get(sub);
            if (dispatcher != null) {
                dispatcher.unsubscribe(sub);
                log.info("Unsubscribed from subject: {}", sub);
            }
        });
    }
}
