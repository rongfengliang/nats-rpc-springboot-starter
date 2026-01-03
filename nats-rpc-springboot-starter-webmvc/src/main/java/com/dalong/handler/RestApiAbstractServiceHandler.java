package com.dalong.handler;

import com.dalong.models.BaseMessage;
import com.dalong.util.NatsRpcCall;
import io.nats.client.impl.Headers;
import org.springframework.http.HttpHeaders;

import java.lang.reflect.Method;

import static com.dalong.util.NatsRpcCall.serializeMessage;

public abstract class RestApiAbstractServiceHandler<T extends BaseMessage> extends AbstractServiceHandler<T> {

    public <R> R defaultRestApiHandler(String serviceName, String prefix,String serviceEndpoint, T demoMessage,   HttpHeaders httpHeaders){
        return restApiHandler(serviceName,prefix,serviceEndpoint, demoMessage, httpHeaders);
    }
    public <R> R restApiHandler(String serviceName, String prefix,String serviceEndpoint,T baseMessage, HttpHeaders headers) {
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
            String fullServiceEndpoint = String.format("%s.svc.%s.%s", serviceName,prefix, serviceEndpoint);
            byte[] payload = serializeMessage(this.getObjectMapper(),baseMessage);
            Object result = NatsRpcCall.call(this.getConnection(),this.getObjectMapper(),fullServiceEndpoint, payload, natsHeaders);
            afterHandle(actionMethod, result, natsHeaders);
            return (R) result;
        }
        return null;
    }
}
