package com.dalong.apihandlers;

import com.dalong.autoconfigure.config.ServiceMapping;
import com.dalong.handler.RestApiAbstractServiceHandler;
import com.dalong.handler.ServiceHandlerType;
import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;


@ServiceHandlerType(
        typeValue = "apigatewayservice",
        version = "1.0.0",
        scope = "global",
        description = "apigatewayservice",
        endpointName = "apigatewayservice",
        messageClass = BaseMessage.class)
@Component
public class NatsRpcServiceHandler extends RestApiAbstractServiceHandler<BaseMessage> {

    private  Connection connection;
    private  ObjectMapper objectMapper;
    public NatsRpcServiceHandler(ObjectMapper objectMapper, Connection connection) {
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
    public BaseMessage defaultMessageHandler(BaseMessage demoMessage, Headers headers) {
        return demoMessage;
    }
    @ResponseBody
    @ServiceMapping(
            name = "defaultRestApiHandler",
            path = {"/api/gw/rest/{servicename}/{prefix}/{serviceendpoint}"},
            method = {"POST"},
            version = "1.0.0"
    )
    @Override
    public Object defaultRestApiHandler(@PathVariable(name = "servicename") String serviceName,
                                        @PathVariable(name = "prefix") String prefix,
                                        @PathVariable(name = "serviceendpoint") String serviceEndpoint,
                                        @RequestBody   BaseMessage demoMessage,
                                        @Parameter(hidden = true) @RequestHeader  HttpHeaders httpHeaders) {
        return super.defaultRestApiHandler(serviceName,prefix,serviceEndpoint, demoMessage, httpHeaders);
    }
}