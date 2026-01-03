package com.dalong.handler;

import com.dalong.helper.MessageHelper;
import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.nats.client.api.Error;
import io.nats.client.impl.Headers;
import io.nats.service.ServiceMessage;
import io.nats.service.ServiceMessageHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dalong.helper.MessageHelper.actionMethodExecute;

public abstract class UnionHandler<T extends BaseMessage> implements ServiceMessageHandler, MessageHandler {
    Map<String, Method> methodCache = new ConcurrentHashMap<>();

    public String serviceEndpointName() {
        return getClass().getAnnotation(UnionHandlerType.class).endpointName();
    }

    public String subjectName() {
        return getClass().getAnnotation(UnionHandlerType.class).subjectName();
    }

    public String serviceVersion() {
        return getClass().getAnnotation(UnionHandlerType.class).version();
    }

    public void registerSubTypes(ObjectMapper objectMapper) {
        objectMapper.registerSubtypes(new NamedType(this.getMessageType(), getClass().getAnnotation(UnionHandlerType.class).typeValue()));
    }

    public String serviceScope() {
        return getClass().getAnnotation(UnionHandlerType.class).scope();
    }

    public Class<T> getMessageType() {
        return (Class<T>) getClass().getAnnotation(UnionHandlerType.class).messageClass();
    }

    public boolean beforeHandle(Method method, T message, Headers headers) {
        return true;
    }

    public void afterHandle(Method method, Object message, Headers headers) {
        // do nothing by default
    }

    public T messageConvert(byte[] data) {
        T baseMessage = MessageHelper.castBaseMessage(data, this.getMessageType(), this.getObjectMapper());
        return baseMessage;
    }

    public abstract Connection getConnection();

    public abstract ObjectMapper getObjectMapper();

    public abstract Object defaultMessageHandler(T message, Headers headers);

    @Override
    public void onMessage(ServiceMessage smsg) {
        T baseMessage = messageConvert(smsg.getData());
        Headers headers = smsg.getHeaders();
        Method actionMethod = actionMethod(baseMessage);
        boolean continueProcess = beforeHandle(actionMethod, baseMessage, headers);
        if (continueProcess) {
            Object result = actionMethodExecute(actionMethod, this, baseMessage, headers);
            try {
                if (headers != null) {
                    smsg.respond(getConnection(), getObjectMapper().writeValueAsBytes(result), headers);
                } else {
                    smsg.respond(getConnection(), getObjectMapper().writeValueAsBytes(result));
                }
                afterHandle(actionMethod, result, headers);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            smsg.respond(getConnection(), Error.JsBadRequestErr);
        }
    }

    @Override
    public void onMessage(Message msg) throws InterruptedException {
        T baseMessage = messageConvert(msg.getData());
        Method actionMethod = actionMethod(baseMessage);
        Headers headers = msg.getHeaders();
        if (beforeHandle(actionMethod, baseMessage, headers)) {
            Object result = actionMethodExecute(actionMethod, this, baseMessage, headers);
            afterHandle(actionMethod, result, headers);
        }
    }

    public Method actionMethod(T message) {
        Method method = null;
        String key = getClass().getName() + "#" + message.getAction();
        if (methodCache.containsKey(key)) {
            return methodCache.get(key);
        } else {
            try {
                Class cls = getClass();
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
