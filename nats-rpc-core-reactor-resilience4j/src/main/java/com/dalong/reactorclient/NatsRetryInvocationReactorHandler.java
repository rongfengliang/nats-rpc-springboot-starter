package com.dalong.reactorclient;

import com.dalong.client.RpcClient;
import com.dalong.client.RpcClientInvocationException;
import com.dalong.helper.SpringEnvironmentHolder;
import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;

@Slf4j
public class NatsRetryInvocationReactorHandler implements InvocationHandler {
    private static final String SERVICE_ENDPOINT_SUBJECT_FORMAT = "%s.svc.%s.%s";

    private final Connection nats;
    private final ObjectMapper objectMapper;
    private final Retry retry;
    private Duration timeout = Duration.ofSeconds(120);

    public NatsRetryInvocationReactorHandler(Connection nats, ObjectMapper objectMapper, Duration timeout, Retry retry) {
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
        if (!(returnType instanceof ParameterizedType parameterizedType)) {
            throw new IllegalStateException("Return type must be parameterized (Mono<T>)");
        }

        Type innerType = parameterizedType.getActualTypeArguments()[0];
        if (innerType == Void.class || innerType == Void.TYPE) {
            if (requestContext.headers != null) {
                nats.publish(requestContext.subject, requestContext.headers, requestContext.payload);
            } else {
                nats.publish(requestContext.subject, requestContext.payload);
            }
            return Mono.empty();
        }

        JavaType javaType = objectMapper.getTypeFactory().constructType(innerType);
        Mono<Object> call = Mono.defer(() -> {
            Mono<io.nats.client.Message> requestMono;
            if (requestContext.headers != null) {
                requestMono = Mono.fromFuture(nats.requestWithTimeout(requestContext.subject, requestContext.headers, requestContext.payload, timeout));
            } else {
                requestMono = Mono.fromFuture(nats.requestWithTimeout(requestContext.subject, requestContext.payload, timeout));
            }

            String serviceNm = service.serviceName();
            if (serviceNm.startsWith("${")) {
                serviceNm = SpringEnvironmentHolder.resolvePlaceholders(serviceNm);
                log.debug("RPC service name resolved to: {}", serviceNm);
            }
            final String serviceName = serviceNm;
            return requestMono
                    .switchIfEmpty(Mono.error(new RpcClientInvocationException(String.format(
                            "RPC retry call failed: service %s no response (subject: %s, timeout: %s seconds)",
                            serviceName, requestContext.subject, timeout.getSeconds()))))
                    .flatMap(message -> {
                        if (message == null) {
                            return Mono.error(new RpcClientInvocationException(String.format(
                                    "RPC retry call failed: service %s no response (subject: %s, timeout: %s seconds)",
                                    serviceName, requestContext.subject, timeout.getSeconds())));
                        }
                        try {
                            return Mono.just(objectMapper.readValue(message.getData(), javaType));
                        } catch (IOException e) {
                            return Mono.error(e);
                        }
                    });
        }).subscribeOn(Schedulers.boundedElastic());

        return call.transformDeferred(RetryOperator.of(retry));
    }

    private RequestContext resolveRequestContext(Method method, Object[] args, RpcClient service) throws Exception {
        RequestContext requestContext = new RequestContext();
        String serviceName = service.serviceName();
        if (serviceName.matches(".*\\$\\{.*\\}.*")) {
            serviceName = SpringEnvironmentHolder.resolvePlaceholders(serviceName);
            log.debug("RPC service name resolved to: {}", serviceName);
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

