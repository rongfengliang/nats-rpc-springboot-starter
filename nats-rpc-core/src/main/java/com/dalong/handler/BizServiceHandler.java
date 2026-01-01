package com.dalong.handler;

import com.dalong.models.BaseMessage;
import io.nats.client.impl.Headers;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务处理器接口,用于处理特定类型的消息
 * @param <T>
 */
public interface BizServiceHandler<T extends BaseMessage>  {
    Map<String, Method> methodCache = new ConcurrentHashMap<>();
    Object defaultMessageHandler(T message,Headers headers);
    Class<T> getMessageType();
    default Method actionMethod(T message) {
        Method method = null;
        String key = getClass().getName() + "#" + message.getAction();
        if (methodCache.containsKey(key)) {
            return methodCache.get(key);
        } else {
            try {
                Class<T> cls = (Class<T>)getClass();
                method = cls.getMethod(message.getAction(), getMessageType(), Headers.class);
                method.setAccessible(true);
                Type returnType = method.getGenericReturnType();
                if (returnType == Void.TYPE) {
                    throw  new NoSuchMethodException("Method returns void, fallback to defaultMessageHandle");
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
