package com.dalong.bean;

import com.dalong.autoconfigure.config.ServiceMapping;
import com.dalong.handler.ServiceHandler;
import com.dalong.util.methodutils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class NatsRpcRestApiRegistor {
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private List<ServiceHandler> serviceHandlers;

    public NatsRpcRestApiRegistor(RequestMappingHandlerMapping requestMappingHandlerMapping, List<ServiceHandler> serviceHandlers) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.serviceHandlers = serviceHandlers;
    }

    public void registerRestApis() {
        serviceHandlers.forEach(serviceHandler -> {
            Class<?> targetClass =
                    org.springframework.aop.support.AopUtils.getTargetClass(serviceHandler);
            Method[] methods = targetClass.getDeclaredMethods();
            Arrays.stream(methods).forEach(method -> {
                ServiceMapping serviceMapping = method.getAnnotation(ServiceMapping.class);
                if (serviceMapping == null) {
                    return;
                }
                if (method.getDeclaringClass() != targetClass) {
                    return;
                }
                RequestMappingInfo mapping = RequestMappingInfo
                        .paths(serviceMapping.path())
                        .methods(Arrays.stream(serviceMapping.method()).map(item -> methodutils.str2RequestMethod(item)).toArray(RequestMethod[]::new))
                        .build();
                Method handlerMethod = method;
                requestMappingHandlerMapping.registerMapping(mapping, serviceHandler, handlerMethod);
            });
        });
    }

}
