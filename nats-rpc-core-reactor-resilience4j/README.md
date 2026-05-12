# nats-rpc-core-reactor-resilience4j

Reactive RPC client extensions for NATS with resilience4j retry support.

## Quick usage

```java
Retry retry = Retry.of("nats-reactor-rpc", RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.ofMillis(200))
        .build());

RpcServiceRetryReactorClient client = new RpcServiceRetryReactorClient(
        natsConnection,
        objectMapper,
        Duration.ofSeconds(2),
        retry
);

DemoService service = client.target(DemoService.class);
Mono<ReplyMessage> result = service.call(request);
```

`NatsRetryInvocationReactorHandler` retries failed request-response calls through resilience4j reactor operators.

