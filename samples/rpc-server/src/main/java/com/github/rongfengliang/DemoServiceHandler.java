package com.github.rongfengliang;

import com.dalong.handler.AbstractServiceHandler;
import com.dalong.handler.ServiceHandlerType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@ServiceHandlerType(
        typeValue = "demo-handler",
        version = "1.0.0",
        scope = "global",
        description = "Demo RPC service handler",
        endpointName = "demo",
        messageClass = DemoMessage.class)
@Component
public class DemoServiceHandler extends AbstractServiceHandler<DemoMessage> {

    private final Connection connection;
    private final ObjectMapper objectMapper;

    public DemoServiceHandler(ObjectMapper objectMapper, Connection connection) {
        this.objectMapper = objectMapper;
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    public Object defaultMessageHandler(DemoMessage message, Headers headers) {
        // Default handler - processes requests when no specific action is specified
        log.info("Received message: {}, headers: {}", message,  headers);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.error("Error sleeping", e);
        }
        log.info("Processed message: {}", message);
        return "Hello, " + message.getName();
    }

    public List<String> getRoles(DemoMessage message, Headers headers) {
        // Custom action method - invoked when message.action = "getRoles"
        log.info("Received message: {}, headers: {}", message,  headers);
        return List.of("admin", "user", "guest");
    }
}