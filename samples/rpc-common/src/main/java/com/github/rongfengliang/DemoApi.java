package com.github.rongfengliang;

import io.nats.client.impl.Headers;

import java.util.List;


public interface DemoApi {
    // Basic call with message only
    String defaultMessageHandler(DemoMessage message);

    // Call with message and headers
    String defaultMessageHandler(DemoMessage message, Headers headers);

    // Call specific action method
    List<String> getRoles(DemoMessage message);

    // Call with custom prefix
    List<String> getRoles(String prefix, DemoMessage message, Headers headers);
}