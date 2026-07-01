package com.github.rongfengliang;

import com.dalong.client.RpcClient;
import io.nats.client.impl.Headers;

import java.util.List;

@RpcClient(
        serviceName = "demo-service",
        servicePrefix = "v1",
        serviceEndpoint = "demo"
)
public interface DemoApiClient extends DemoApi{
    String defaultMessageHandler(DemoMessage message);

    // Call with message and headers
    String defaultMessageHandler(DemoMessage message, Headers headers);

    // Call specific action method
    List<String> getRoles(DemoMessage message);

    // Call with custom prefix
    List<String> getRoles(String prefix, DemoMessage message, Headers headers);

}
