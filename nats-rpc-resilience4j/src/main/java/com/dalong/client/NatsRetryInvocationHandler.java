package com.dalong.client;

import com.dalong.helper.SpringEnvironmentHolder;
import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.Retry;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Drop-in InvocationHandler variant that adds resilience4j retry around NATS request/response calls.
 */
public class NatsRetryInvocationHandler implements InvocationHandler {
    private static final String SERVICE_ENDPOINT_SUBJECT_FORMAT = "%s.svc.%s.%s";

    private final Connection nats;
    private final ObjectMapper objectMapper;
    private final Retry retry;
    private Duration timeout = Duration.ofSeconds(120);

    public NatsRetryInvocationHandler(Connection nats, ObjectMapper objectMapper, Duration timeout, Retry retry) {
        this.nats = nats;
        this.objectMapper = objectMapper;
        this.retry = retry;
        if (timeout != null) {
            this.timeout = timeout;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        Object[] safeArgs = args == null ? new Object[0] : args;
        Class<?> clazz = method.getDeclaringClass();
        RpcClient service = clazz.getAnnotation(RpcClient.class);
        if (service == null) {
            throw new RpcClientInvocationException("RPC client interface is missing @RpcClient annotation: " + clazz.getName());
        }

        RequestContext requestContext = resolveRequestContext(method, safeArgs, service);
        Type returnType = method.getGenericReturnType();
        if (returnType == Void.TYPE) {
            if (requestContext.headers != null) {
                nats.publish(requestContext.subject, requestContext.headers, requestContext.payload);
            } else {
                nats.publish(requestContext.subject, requestContext.payload);
            }
            return null;
        }

        JavaType javaType = objectMapper.getTypeFactory().constructType(returnType);
        Callable<Object> call = Retry.decorateCallable(retry, () -> {
            Message message;
            if (requestContext.headers != null) {
                message = nats.request(requestContext.subject, requestContext.headers, requestContext.payload, timeout);
            } else {
                message = nats.request(requestContext.subject, requestContext.payload, timeout);
            }

            if (message == null) {
                String serviceName = service.serviceName();

                throw new RpcClientInvocationException(String.format(
                        "RPC retry call failed: service %s no response (subject: %s, timeout: %s seconds)",
                        serviceName, requestContext.subject, timeout.getSeconds()));
            }
            return objectMapper.readValue(message.getData(), javaType);
        });
        return call.call();
    }

    private RequestContext resolveRequestContext(Method method, Object[] args, RpcClient service) throws Exception {
        RequestContext requestContext = new RequestContext();
        String serviceName = service.serviceName();
        if (serviceName.matches(".*\\$\\{.*\\}.*")) {
            serviceName = SpringEnvironmentHolder.resolvePlaceholders(serviceName);
        }
        if (args.length == 3 && args[0] instanceof String) {
            requestContext.subject = String.format(
                    SERVICE_ENDPOINT_SUBJECT_FORMAT,
                    serviceName,
                    args[0],
                    service.serviceEndpoint()
            );
            BaseMessage msg = (BaseMessage) args[1];
            msg.setAction(method.getName());
            requestContext.payload = objectMapper.writeValueAsBytes(msg);
            requestContext.headers = (Headers) args[2];
            return requestContext;
        }

        if (args.length == 2 && args[0] instanceof String) {
            requestContext.subject = String.format(
                    SERVICE_ENDPOINT_SUBJECT_FORMAT,
                    serviceName,
                    args[0],
                    service.serviceEndpoint()
            );
            BaseMessage msg = (BaseMessage) args[1];
            msg.setAction(method.getName());
            requestContext.payload = objectMapper.writeValueAsBytes(msg);
            return requestContext;
        }

        if (args.length == 2 && !(args[0] instanceof String)) {
            requestContext.subject = String.format(
                    SERVICE_ENDPOINT_SUBJECT_FORMAT,
                    serviceName,
                    service.servicePrefix(),
                    service.serviceEndpoint()
            );
            BaseMessage msg = (BaseMessage) args[0];
            msg.setAction(method.getName());
            requestContext.payload = objectMapper.writeValueAsBytes(msg);
            requestContext.headers = (Headers) args[1];
            return requestContext;
        }

        if (args.length == 1) {
            requestContext.subject = String.format(
                    SERVICE_ENDPOINT_SUBJECT_FORMAT,
                    serviceName,
                    service.servicePrefix(),
                    service.serviceEndpoint()
            );
            BaseMessage msg = (BaseMessage) args[0];
            msg.setAction(method.getName());
            requestContext.payload = objectMapper.writeValueAsBytes(msg);
            return requestContext;
        }

        throw new RpcClientInvocationException("Unsupported RPC argument format for method: " + method.getName());
    }

    private static class RequestContext {
        private String subject;
        private byte[] payload;
        private Headers headers;
    }
}

