package com.dalong.reactorclient;

import com.dalong.client.RpcClient;
import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class NatsInvocationReactorHandler implements InvocationHandler {
    private final Connection nats;
    private final ObjectMapper objectMapper;
    private Duration timeout = Duration.ofSeconds(120);

    public NatsInvocationReactorHandler(Connection nats, ObjectMapper objectMapper, Duration timeout) {
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
        Class<?> clazz = method.getDeclaringClass();
        RpcClient service = clazz.getAnnotation(RpcClient.class);
        String serviceEndpointSubjectFormatt = "%s.svc.%s.%s";
        String subject = "";
        byte[] req = null;
        Headers headers = null;
        if (args.length == 3 && (args[0] instanceof String)) {
            subject = String.format(
                    serviceEndpointSubjectFormatt,
                    service.serviceName(),
                    (String) args[0],
                    service.serviceEndpoint()
            );
            BaseMessage msg = (BaseMessage) args[1];
            // 每次直接覆盖action,使用方法名作为action,注意会覆盖掉之前的action值, 造成BaseMessage的action字段信息不太正确,此问题只存在于rpc调用场景
            // 理想情况，应该使用深拷贝来避免修改原始对象，但为了性能考虑，这里直接修改原始对象
            msg.setAction(method.getName());
            req = objectMapper.writeValueAsBytes(msg);
            headers = (Headers) args[2];
        }
        if (args.length == 2 && (args[0] instanceof String)) {
            subject = String.format(
                    serviceEndpointSubjectFormatt,
                    service.serviceName(),
                    (String) args[0],
                    service.serviceEndpoint()
            );
            BaseMessage msg = (BaseMessage) args[1];
            // 每次直接覆盖action,使用方法名作为action,注意会覆盖掉之前的action值, 造成BaseMessage的action字段信息不太正确,此问题只存在于rpc调用场景
            // 理想情况，应该使用深拷贝来避免修改原始对象，但为了性能考虑，这里直接修改原始对象
            msg.setAction(method.getName());
            req = objectMapper.writeValueAsBytes(msg);
        }

        if (args.length == 2 && (args[0] instanceof String) == false) {
            subject = String.format(
                    serviceEndpointSubjectFormatt,
                    service.serviceName(),
                    service.servicePrefix(),
                    service.serviceEndpoint()
            );
            BaseMessage msg = (BaseMessage) args[0];
            // 每次直接覆盖action,使用方法名作为action,注意会覆盖掉之前的action值, 造成BaseMessage的action字段信息不太正确,此问题只存在于rpc调用场景
            // 理想情况，应该使用深拷贝来避免修改原始对象，但为了性能考虑，这里直接修改原始对象
            msg.setAction(method.getName());
            req = objectMapper.writeValueAsBytes(msg);
            headers = (Headers) args[1];
        }
        if (args.length == 1) {
            subject = String.format(
                    serviceEndpointSubjectFormatt,
                    service.serviceName(),
                    service.servicePrefix(),
                    service.serviceEndpoint()

            );
            BaseMessage msg = (BaseMessage) args[0];
            // 每次直接覆盖action,使用方法名作为action,注意会覆盖掉之前的action值, 造成BaseMessage的action字段信息不太正确,此问题只存在于rpc调用场景
            // 理想情况，应该使用深拷贝来避免修改原始对象，但为了性能考虑，这里直接修改原始对象
            msg.setAction(method.getName());
            req = objectMapper.writeValueAsBytes(msg);
        }
        // 0 代表数据，1 代表Headers
        Type returnType = method.getGenericReturnType();
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException("Return type must be parameterized (Mono<T>)");
        }
        ParameterizedType parameterizedType = (ParameterizedType) returnType;
        Type innerType = parameterizedType.getActualTypeArguments()[0];
        JavaType javaType = objectMapper.getTypeFactory()
                .constructType(innerType);
        if (returnType == Void.TYPE) {
            // 为void类型，不需要等待响应,直接发送消息
            return null;
        }
        CompletableFuture<Message> msg;

        if (headers != null) {
            msg = nats.requestWithTimeout(subject, headers, req,timeout);
        } else {
            msg = nats.requestWithTimeout(subject, req,timeout);
        }
        Mono<Object> monoResult = Mono.fromFuture(msg)
                // 放在弹性线程池上执行，以避免阻塞事件循环
                .subscribeOn(Schedulers.boundedElastic())
                // 异步反序列化
                .flatMap(data -> {
                    Object value = null;
                    try {
                        value = objectMapper.readValue(data.getData(), javaType);
                        return Mono.just(value);
                    } catch (IOException e) {
                       return Mono.error(e);
                    }
                });
        return  monoResult;
    }
}
