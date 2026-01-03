package com.dalong.autoconfigure.service;

import com.dalong.autoconfigure.config.RpcServiceConfig;
import com.dalong.handler.ServiceHandler;
import com.dalong.handler.UnionHandler;
import com.dalong.registry.ServiceHandlerRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.service.Endpoint;
import io.nats.service.Service;
import io.nats.service.ServiceBuilder;
import io.nats.service.ServiceEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RegisterUnionNatsService {

    private Service service;
    private Map<String, Dispatcher> dispatcherMap = new ConcurrentHashMap<>();

    public RegisterUnionNatsService(List<UnionHandler> serviceHandlers, Connection connection, RpcServiceConfig config, ObjectMapper objectMapper) {
        // Build NATS Service
        String serviceName = config.getRpcServiceName();
        String serviceVersion = config.getRpcServiceVersion();
        String servicePrefix = config.getRpcServicePrefix();
        String serviceEndpointFormat = "%s-%s-%s";
        String serviceEndpointSubjectFormatt = "%s.svc.%s.%s";
        String serviceMsgSubjectFormat = "%s.msg.%s.%s";
        Dispatcher dispatcher = connection.createDispatcher();
        ServiceBuilder serviceBuilder = new ServiceBuilder().connection(connection).version(serviceVersion).name(serviceName);
        serviceHandlers.forEach(serviceHandler -> {
            String endpointName = String.format(serviceEndpointFormat, serviceName, servicePrefix, serviceHandler.serviceEndpointName());
            String serviceEndpointSubject = String.format(serviceEndpointSubjectFormatt, serviceName, servicePrefix, serviceHandler.serviceEndpointName());
            Endpoint endpoint = new Endpoint.Builder()
                    .subject(serviceEndpointSubject)
                    .name("nats-rpc-service-endpoint-" + serviceHandler.serviceEndpointName())
                    .build();
            ServiceEndpoint serviceEndpoint = ServiceEndpoint.builder()
                    .endpoint(endpoint)
                    .endpointName(endpointName)
                    .handler(serviceHandler)
                    .build();
            serviceHandler.registerSubTypes(objectMapper);
            String subject = String.format(serviceMsgSubjectFormat, serviceName, servicePrefix, serviceHandler.subjectName());
            log.info("Subscribing  subject: {} with type: {}", subject, serviceHandler.getMessageType());
            dispatcher.subscribe(subject, "messagequeue", serviceHandler);
            dispatcherMap.put(subject, dispatcher);
            log.info("Registering service:  endpoint: {}, with type: {}", serviceEndpointSubject, serviceHandler.getMessageType());
            serviceBuilder.addServiceEndpoint(serviceEndpoint);
        });
        this.service = serviceBuilder.build();
    }

    public void start() throws Exception {
        if (service != null) {
            service.startService();
        }

    }

    public void stop() throws Exception {
        if (service != null) {
            service.stop(true);
        }
        dispatcherMap.keySet().forEach(sub -> {
            Dispatcher dispatcher = dispatcherMap.get(sub);
            if (dispatcher != null) {
                dispatcher.unsubscribe(sub);
                log.info("Unsubscribed from subject: {}", sub);
            }
        });
    }
}
