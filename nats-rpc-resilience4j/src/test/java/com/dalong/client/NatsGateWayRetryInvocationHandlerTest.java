package com.dalong.client;

import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NatsGateWayRetryInvocationHandlerTest {

    @Test
    void shouldRetryGatewayRequestWhenFirstResponseIsNull() throws Exception {
        Connection connection = mock(Connection.class);
        Message natsMessage = mock(Message.class);
        ObjectMapper mapper = new ObjectMapper();

        ReplyMessage reply = new ReplyMessage();
        reply.result = "ok";

        when(natsMessage.getData()).thenReturn(mapper.writeValueAsBytes(reply));
        when(connection.request(any(String.class), any(Headers.class), any(byte[].class), any(Duration.class)))
                .thenReturn(null)
                .thenReturn(natsMessage);

        Retry retry = Retry.of("nats-gateway-rpc", RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ZERO)
                .build());

        DemoGateway client = RpcServiceGateWayRetryProxy.create(
                DemoGateway.class,
                connection,
                mapper,
                Duration.ofSeconds(1),
                retry
        );

        NatsMethod method = new NatsMethod();
        method.setServiceName("demo");
        method.setServicePrefix("global");
        method.setServiceEndpoint("rpc");

        RequestMessage request = new RequestMessage();
        request.setType("request");
        ReplyMessage result = client.gateway(method, request, new Headers());

        Assertions.assertEquals("ok", result.result);
        verify(connection, times(2)).request(any(String.class), any(Headers.class), any(byte[].class), any(Duration.class));
    }

    private interface DemoGateway {
        ReplyMessage gateway(@RpcMethod NatsMethod method, @RpcPayload RequestMessage request, @RpcHeaders Headers headers);
    }

    private static class RequestMessage extends BaseMessage {
        public String payload;
    }

    private static class ReplyMessage extends BaseMessage {
        public String result;
    }
}

