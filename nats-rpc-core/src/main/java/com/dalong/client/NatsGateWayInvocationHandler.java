package com.dalong.client;

import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.time.Duration;

public class NatsGateWayInvocationHandler implements InvocationHandler {
    private static final String SERVICE_ENDPOINT_SUBJECT_FORMAT = "%s.svc.%s.%s";
    private final Connection nats;
    private final ObjectMapper objectMapper;
    private Duration timeout = Duration.ofSeconds(120);

    public NatsGateWayInvocationHandler(Connection nats, ObjectMapper objectMapper, Duration timeout) {
        this.nats = nats;
        this.objectMapper = objectMapper;
        if (timeout != null) {
            this.timeout = timeout;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        ResolvedInvocation resolvedInvocation = resolveRequestContext(method, args);
        RequestContext requestContext = resolvedInvocation.requestContext;
        Type returnType = method.getGenericReturnType();
        if (returnType == Void.TYPE) {
            if (requestContext.headers != null) {
                nats.publish(resolvedInvocation.subject, requestContext.headers, requestContext.payload);
            } else {
                nats.publish(resolvedInvocation.subject, requestContext.payload);
            }
            return null;
        }

        Message message;
        if (requestContext.headers != null) {
            message = nats.request(resolvedInvocation.subject, requestContext.headers, requestContext.payload, timeout);
        } else {
            message = nats.request(resolvedInvocation.subject, requestContext.payload, timeout);
        }

        if (message == null) {
            throw new RpcClientInvocationException(String.format(
                    "RPC 调用失败: 服务 %s 未响应 (subject: %s, timeout: %s秒)",
                    resolvedInvocation.rpcMethod.getServiceName(), resolvedInvocation.subject, timeout.getSeconds()));
        }

        JavaType javaType = resolvedInvocation.responseJavaType == null
                ? objectMapper.getTypeFactory().constructType(returnType)
                : resolvedInvocation.responseJavaType;
        return objectMapper.readValue(message.getData(), javaType);
    }

    private ResolvedInvocation resolveRequestContext(Method method, Object[] args) throws Exception {
        Object[] safeArgs = args == null ? new Object[0] : args;
        Parameter[] parameters = method.getParameters();
        if (parameters.length != safeArgs.length) {
            throw new RpcClientInvocationException("方法参数与调用参数数量不匹配: " + method.getName());
        }

        ResolvedInvocation resolvedInvocation = resolveAnnotatedRequestContext(method, parameters, safeArgs);
        RequestContext requestContext = resolvedInvocation.requestContext;
        ensureRpcMethod(resolvedInvocation.rpcMethod, method);

        if (requestContext.messagePayload instanceof BaseMessage baseMessage) {
            baseMessage.setAction(resolveAction(method, resolvedInvocation.rpcMethod));
        }

        requestContext.payload = requestContext.messagePayload == null
                ? new byte[0]
                : objectMapper.writeValueAsBytes(requestContext.messagePayload);
        resolvedInvocation.subject = buildSubject(resolvedInvocation.rpcMethod);
        return resolvedInvocation;
    }

    private ResolvedInvocation resolveAnnotatedRequestContext(Method method, Parameter[] parameters, Object[] args) {
        RequestContext requestContext = new RequestContext();
        ResolvedInvocation resolvedInvocation = new ResolvedInvocation();
        resolvedInvocation.requestContext = requestContext;
        String prefixOverride = null;

        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            Object arg = args[index];
            boolean handled = false;

            for (Annotation annotation : parameter.getAnnotations()) {
                if (annotation instanceof RpcMethod) {
                    ensureSingleRpcMethod(resolvedInvocation, method);
                    resolvedInvocation.rpcMethod = castRpcMethod(arg, method, index);
                    if (prefixOverride != null) {
                        resolvedInvocation.rpcMethod.setServicePrefix(prefixOverride);
                    }
                    handled = true;
                }
                if (annotation instanceof Prefix prefixAnnotation) {
                    ensurePrefixParameter(method, parameter, index);
                    ensureSinglePrefix(requestContext, method);
                    prefixOverride = resolvePrefixValue(prefixAnnotation, arg, resolvedInvocation.rpcMethod == null ? null : resolvedInvocation.rpcMethod.getServicePrefix());
                    if (resolvedInvocation.rpcMethod != null) {
                        resolvedInvocation.rpcMethod.setServicePrefix(prefixOverride);
                    }
                    requestContext.prefixResolved = true;
                    handled = true;
                }
                if (annotation instanceof RpcHeaders) {
                    ensureSingleHeaders(requestContext, method);
                    requestContext.headers = castHeaders(arg, method, index);
                    handled = true;
                }
                if (annotation instanceof RpcPayload) {
                    ensureSinglePayload(requestContext, method);
                    requestContext.messagePayload = arg;
                    handled = true;
                }
            }

            if (handled) {
                continue;
            }

            if (arg instanceof Headers) {
                ensureSingleHeaders(requestContext, method);
                requestContext.headers = (Headers) arg;
                continue;
            }

            if (isResponseTypeParameter(parameter)) {
                setResponseType(resolvedInvocation, arg, method, index);
                continue;
            }

            if (requestContext.messagePayload == null && !(arg instanceof NatsMethod)) {
                requestContext.messagePayload = arg;
                continue;
            }

            throw new RpcClientInvocationException("无法解析 Gateway 方法参数: " + method.getName() + " 第" + (index + 1) + "个参数");
        }

        return resolvedInvocation;
    }

    private boolean isResponseTypeParameter(Parameter parameter) {
        return Class.class.equals(parameter.getType()) || TypeReference.class.isAssignableFrom(parameter.getType());
    }

    private void setResponseType(ResolvedInvocation resolvedInvocation, Object arg, Method method, int index) {
        ensureSingleResponseType(resolvedInvocation, method);
        if (arg == null) {
            throw new RpcClientInvocationException("Gateway 方法 " + method.getName() + " 的第" + (index + 1) + "个返回类型参数不能为 null");
        }
        if (arg instanceof Class<?> clazz) {
            resolvedInvocation.responseJavaType = objectMapper.getTypeFactory().constructType(clazz);
            return;
        }
        if (arg instanceof TypeReference<?> typeReference) {
            resolvedInvocation.responseJavaType = objectMapper.getTypeFactory().constructType(typeReference.getType());
            return;
        }
        throw new RpcClientInvocationException("Gateway 方法 " + method.getName() + " 的第" + (index + 1) + "个参数必须是 Class 或 TypeReference 类型");
    }

    private void ensureRpcMethod(NatsMethod rpcMethod, Method method) {
        if (rpcMethod == null) {
            throw new RpcClientInvocationException("Gateway 方法必须包含一个 @RpcMethod NatsMethod 参数: " + method.getName());
        }
        if (isBlank(rpcMethod.getServiceName())) {
            throw new RpcClientInvocationException("Gateway 方法缺少 serviceName: " + method.getName());
        }
        if (isBlank(rpcMethod.getServiceEndpoint())) {
            throw new RpcClientInvocationException("Gateway 方法缺少 serviceEndpoint: " + method.getName());
        }
    }

    private Headers castHeaders(Object arg, Method method, int index) {
        if (arg == null) {
            return null;
        }
        if (arg instanceof Headers headers) {
            return headers;
        }
        throw new RpcClientInvocationException("Gateway 方法 " + method.getName() + " 的第" + (index + 1) + "个参数不是 Headers 类型");
    }

    private NatsMethod castRpcMethod(Object arg, Method method, int index) {
        if (arg instanceof NatsMethod rpcMethod) {
            return rpcMethod;
        }
        throw new RpcClientInvocationException("Gateway 方法 " + method.getName() + " 的第" + (index + 1) + "个参数必须是 NatsMethod 类型");
    }

    private void ensurePrefixParameter(Method method, Parameter parameter, int index) {
        if (!String.class.equals(parameter.getType())) {
            throw new RpcClientInvocationException("Gateway 方法 " + method.getName() + " 的第" + (index + 1) + "个 @Prefix 参数必须是 String 类型");
        }
    }

    private String resolvePrefixValue(Prefix prefixAnnotation, Object arg, String defaultPrefix) {
        if (arg instanceof String prefix && !prefix.isBlank()) {
            return prefix;
        }
        if (!prefixAnnotation.value().isBlank()) {
            return prefixAnnotation.value();
        }
        return defaultPrefix;
    }

    private void ensureSinglePayload(RequestContext requestContext, Method method) {
        if (requestContext.messagePayload != null) {
            throw new RpcClientInvocationException("Gateway 方法存在多个 payload 参数: " + method.getName());
        }
    }

    private void ensureSingleHeaders(RequestContext requestContext, Method method) {
        if (requestContext.headers != null) {
            throw new RpcClientInvocationException("Gateway 方法存在多个 headers 参数: " + method.getName());
        }
    }

    private void ensureSinglePrefix(RequestContext requestContext, Method method) {
        if (requestContext.prefixResolved) {
            throw new RpcClientInvocationException("Gateway 方法存在多个 prefix 参数: " + method.getName());
        }
    }

    private void ensureSingleRpcMethod(ResolvedInvocation resolvedInvocation, Method method) {
        if (resolvedInvocation.rpcMethod != null) {
            throw new RpcClientInvocationException("Gateway 方法存在多个 @RpcMethod 参数: " + method.getName());
        }
    }

    private void ensureSingleResponseType(ResolvedInvocation resolvedInvocation, Method method) {
        if (resolvedInvocation.responseJavaType != null) {
            throw new RpcClientInvocationException("Gateway 方法存在多个返回类型参数(Class/TypeReference): " + method.getName());
        }
    }

    private String resolveAction(Method method, NatsMethod rpcMethod) {
        if (rpcMethod != null && !isBlank(rpcMethod.getAction())) {
            return rpcMethod.getAction();
        }
        return method.getName();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String buildSubject(NatsMethod rpcMethod) {
        return String.format(
                SERVICE_ENDPOINT_SUBJECT_FORMAT,
                rpcMethod.getServiceName(),
                isBlank(rpcMethod.getServicePrefix()) ? "global" : rpcMethod.getServicePrefix(),
                rpcMethod.getServiceEndpoint());
    }

    private static class RequestContext {
        private boolean prefixResolved;
        private Object messagePayload;
        private Headers headers;
        private byte[] payload;
    }

    private static class ResolvedInvocation {
        private String subject;
        private NatsMethod rpcMethod;
        private RequestContext requestContext;
        private JavaType responseJavaType;
    }
}
