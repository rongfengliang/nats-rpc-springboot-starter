package com.dalong.apihandlers;

import com.dalong.autoconfigure.config.UnionMapping;
import com.dalong.handler.UnionHandlerType;
import com.dalong.handler.UnionRestApiAbstractServiceHandler;
import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;


@UnionHandlerType(
        typeValue = "apigatewayservice",
        version = "1.0.0",
        scope = "global",
        endpointName = "apigatewayservicev2",
        description = "apigatewayservicer",
        subjectName = "apigatewayservicev2",
        messageClass = BaseMessage.class)
@Component
public class NatsUnionServiceHandler extends UnionRestApiAbstractServiceHandler<BaseMessage> {

    private Connection connection;
    private ObjectMapper objectMapper;

    public NatsUnionServiceHandler(ObjectMapper objectMapper, Connection connection) {
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
    @UnionMapping(
            name = "defaultMsgApiHandler",
            path = {"/api/gw/v2/msg/{servicename}/{prefix}/{serviceendpoint}"},
            method = {"POST"},
            version = "1.0.0"
    )
    @Override
    public Mono<ResponseEntity<Object>> defaultMsgApiHandler(@PathVariable(name = "servicename") String serviceName,
                                                             @PathVariable(name = "prefix") String prefix,
                                                             @PathVariable(name = "serviceendpoint") String serviceEndpoint,
                                                             @RequestBody BaseMessage demoMessage,
                                                             @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders) {
        var resul = super.defaultMsgApiHandler(serviceName, prefix, serviceEndpoint, demoMessage, httpHeaders);
        return Mono.just(ResponseEntity.ok(resul));
    }

    @ResponseBody
    @UnionMapping(
            name = "defaultRestApiHandler",
            path = {"/api/gw/v2/rest/{servicename}/{prefix}/{serviceendpoint}"},
            method = {"POST"},
            version = "1.0.0"
    )
    @Override
    public Mono<Object> defaultRestApiHandler(@PathVariable(name = "servicename") String serviceName,
                                        @PathVariable(name = "prefix") String prefix,
                                        @PathVariable(name = "serviceendpoint") String serviceEndpoint,
                                        @RequestBody BaseMessage demoMessage,
                                        @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders) {
        return Mono.just(super.defaultRestApiHandler(serviceName, prefix, serviceEndpoint, demoMessage, httpHeaders));
    }
}