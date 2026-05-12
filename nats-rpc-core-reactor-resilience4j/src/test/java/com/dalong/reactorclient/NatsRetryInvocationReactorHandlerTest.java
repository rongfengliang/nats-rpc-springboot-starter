package com.dalong.reactorclient;

import com.dalong.client.RpcClient;
import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.nats.client.Connection;
import io.nats.client.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NatsRetryInvocationReactorHandlerTest {

    @Test
    void shouldRetryWhenNoResponseThenSucceed() throws Exception {
        Connection connection = mock(Connection.class);
        Message natsMessage = mock(Message.class);
        ObjectMapper mapper = new ObjectMapper();

        ReplyMessage reply = new ReplyMessage();
        reply.result = "ok";

        when(natsMessage.getData()).thenReturn(mapper.writeValueAsBytes(reply));
        when(connection.requestWithTimeout(anyString(), any(byte[].class), any(Duration.class)))
                .thenReturn(CompletableFuture.completedFuture(null))
                .thenReturn(CompletableFuture.completedFuture(natsMessage));

        Retry retry = Retry.of("nats-reactor-rpc", RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ZERO)
                .build());

        DemoService client = RpcServiceRetryReactorProxy.create(
                DemoService.class,
                connection,
                mapper,
                Duration.ofSeconds(1),
                retry
        );

        RequestMessage request = new RequestMessage();
        request.setType("request");
        ReplyMessage result = client.call(request).block(Duration.ofSeconds(3));

        Assertions.assertNotNull(result);
        Assertions.assertEquals("ok", result.result);
        verify(connection, times(2)).requestWithTimeout(anyString(), any(byte[].class), any(Duration.class));
    }

    @RpcClient(serviceName = "demo", serviceEndpoint = "rpc")
    private interface DemoService {
        Mono<ReplyMessage> call(RequestMessage request);
    }

    private static class RequestMessage extends BaseMessage {
        public String payload;
    }

    private static class ReplyMessage extends BaseMessage {
        public String result;
    }
}

