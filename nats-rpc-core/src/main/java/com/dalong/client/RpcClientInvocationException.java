package com.dalong.client;

public class RpcClientInvocationException extends RuntimeException {
    public RpcClientInvocationException(String message) {
        super(message);
    }

    public RpcClientInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}

