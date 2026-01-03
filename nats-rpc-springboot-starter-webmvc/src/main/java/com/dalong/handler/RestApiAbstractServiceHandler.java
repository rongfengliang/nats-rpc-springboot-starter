package com.dalong.handler;

import com.dalong.models.BaseMessage;
import com.dalong.util.NatsRpcCall;
import io.nats.client.impl.Headers;
import org.springframework.http.HttpHeaders;

import static com.dalong.util.NatsRpcCall.serializeMessage;

public abstract class RestApiAbstractServiceHandler<T extends BaseMessage> extends AbstractServiceHandler<T> {

    public Object defaultRestApiHandler(String serviceName, String prefix, String serviceEndpoint, T demoMessage, HttpHeaders httpHeaders) {
        return restApiHandler(serviceName, prefix, serviceEndpoint, demoMessage, httpHeaders);
    }

    public boolean beforeRestApiHandler(String serviceName, String prefix, String serviceEndpoint, T baseMessage, HttpHeaders headers) {
        return true;
    }

    public void afterRestApiHandler(String serviceName, String prefix, String serviceEndpoint, T baseMessage, HttpHeaders headers, Object result) {

    }

    public Object restApiHandler(String serviceName, String prefix, String serviceEndpoint, T baseMessage, HttpHeaders headers) {
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
        String fullServiceEndpoint = String.format("%s.svc.%s.%s", serviceName, prefix, serviceEndpoint);
        if (beforeRestApiHandler(serviceName, prefix, serviceEndpoint, baseMessage, headers)) {
            byte[] payload = serializeMessage(this.getObjectMapper(), baseMessage);
            Object result = NatsRpcCall.call(this.getConnection(), this.getObjectMapper(), fullServiceEndpoint, payload, natsHeaders);
            afterRestApiHandler(serviceName, prefix, serviceEndpoint, baseMessage, headers, result);
            return result;
        }
        return null;
    }
}
