package com.dalong.autoconfigure.service;

import com.dalong.handler.ServiceHandler;
import com.dalong.registry.ServiceHandlerRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dalong.autoconfigure.config.RpcServiceConfig;
import io.nats.client.Connection;
import io.nats.service.Endpoint;
import io.nats.service.Service;
import io.nats.service.ServiceBuilder;
import io.nats.service.ServiceEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RegisterRpcService {

    private Service service;
    public RegisterRpcService(List<ServiceHandler> serviceHandlers, Connection connection, RpcServiceConfig config, ServiceHandlerRegistry serviceHandlerRegistry, ObjectMapper objectMapper) {
        String serviceName  = config.getRpcServiceName();
        String serviceVersion = config.getRpcServiceVersion();
        String servicePrefix = config.getRpcServicePrefix();
        String serviceEndpointFormat = "%s-%s-%s";
        String serviceEndpointSubjectFormatt = "%s.svc.%s.%s";
        ServiceBuilder serviceBuilder = new ServiceBuilder().connection(connection).version(serviceVersion).name(serviceName);
        serviceHandlers.forEach(serviceHandler -> {
            String endpointName = String.format(serviceEndpointFormat, serviceName, servicePrefix, serviceHandler.serviceEndpointName());
            String serviceEndpointSubject = String.format(serviceEndpointSubjectFormatt, serviceName, servicePrefix, serviceHandler.serviceEndpointName());
            Endpoint endpoint = new Endpoint.Builder()
                    .subject(serviceEndpointSubject)
                    .name("zaojian-rpc-service-endpoint-" + serviceHandler.serviceEndpointName())
                    .build();
            ServiceEndpoint serviceEndpoint = ServiceEndpoint.builder()
                    .endpoint(endpoint)
                    .endpointName(endpointName)
                    .handler(serviceHandler)
                    .build();
            serviceHandler.registerSubTypes(objectMapper);
            serviceHandlerRegistry.register(serviceHandler);
            log.info("Registering service:  endpoint: {}, with type: {}",serviceEndpointSubject,serviceHandler.getMessageType());
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
    }
}
