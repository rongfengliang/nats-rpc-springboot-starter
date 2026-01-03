package com.dalong.handler;

import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import io.nats.client.impl.Headers;
import io.nats.service.ServiceMessageHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface ServiceHandler<T extends BaseMessage> extends ServiceMessageHandler {
    Map<String, Method> methodCache = new ConcurrentHashMap<>();

    default String serviceEndpointName() {
        return getClass().getAnnotation(ServiceHandlerType.class).endpointName();
    }

    default String serviceVersion() {
        return getClass().getAnnotation(ServiceHandlerType.class).version();
    }

    default void registerSubTypes(ObjectMapper objectMapper) {
        objectMapper.registerSubtypes(new NamedType(this.getMessageType(), getClass().getAnnotation(ServiceHandlerType.class).typeValue()));
    }

    default Object defaultMessageHandler(T message, Headers headers) {
        return null;
    }

    ;

    T messageConvert(byte[] data);

    default String serviceScope() {
        return getClass().getAnnotation(ServiceHandlerType.class).scope();
    }

    default Class<T> getMessageType() {
        return (Class<T>) getClass().getAnnotation(ServiceHandlerType.class).messageClass();
    }

    /**
     * 消息处理前置方法,返回false则不继续处理,返回true继续处理,默认返回true
     * 可以用于权限校验等场景
     *
     * @param message
     * @param headers
     * @return
     */
    default boolean beforeHandle(Method method, T message, Headers headers) {
        return true;
    }

    /**
     * 消息处理后置方法,默认不做任何操作
     * 可以用于日志记录等场景,注意此方法最好不要抛出异常，以免影响主流程，同时也推荐使用异步处理
     *
     * @param message
     * @param headers
     */
    default void afterHandle(Method method, Object message, Headers headers) {
        // do nothing by default
    }

    default Method actionMethod(T message) {
        Method method = null;
        String key = getClass().getName() + "#" + message.getAction();
        if (methodCache.containsKey(key)) {
            return methodCache.get(key);
        } else {
            try {
                Class<T> cls = (Class<T>) getClass();
                method = cls.getMethod(message.getAction(), getMessageType(), Headers.class);
                method.setAccessible(true);
                Type returnType = method.getGenericReturnType();
                if (returnType == Void.TYPE) {
                    throw new NoSuchMethodException("Method returns void, fallback to defaultMessageHandle");
                }
                methodCache.put(key, method);
                return method;
            } catch (NoSuchMethodException e) {
                // fallback to defaultMessageHandle if method not found
                try {
                    method = getClass().getMethod("defaultMessageHandler", message.getClass(), Headers.class);
                    method.setAccessible(true);
                } catch (NoSuchMethodException ex) {
                    // This should not happen, as defaultMessageHandle is expected to be implemented, if implementing interface.should carfully check
                }
                methodCache.put(key, method);
                return method;
            }
        }
    }
}
