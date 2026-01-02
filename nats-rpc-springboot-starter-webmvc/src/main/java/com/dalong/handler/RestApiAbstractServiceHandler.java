package com.dalong.handler;

import com.dalong.models.BaseMessage;
import io.nats.client.impl.Headers;
import org.springframework.http.HttpHeaders;

import java.lang.reflect.Method;

import static com.dalong.helper.MessageHelper.actionMethodExecute;

public abstract class RestApiAbstractServiceHandler<T extends BaseMessage> extends AbstractServiceHandler<T> {

    public <R> R defaultRestApiHandler(T demoMessage,   HttpHeaders httpHeaders){
        return restApiHandler(demoMessage, httpHeaders);
    }
    public <R> R restApiHandler(T baseMessage, HttpHeaders headers) {
        Method actionMethod = actionMethod(baseMessage);
        Headers natsHeaders;
        if (headers != null) {
            natsHeaders = new Headers();
            headers.forEach((key, values) -> {
                for (String value : values) {
                    natsHeaders.add(key, value);
                }
            });
        } else {
            natsHeaders = null;
        }
        boolean continueProcess = beforeHandle(actionMethod, baseMessage, natsHeaders);
        if (continueProcess) {
            Object result = actionMethodExecute(actionMethod, this, baseMessage, natsHeaders);
            afterHandle(actionMethod, result, natsHeaders);
            return (R) result;
        }
        return null;
    }
}
