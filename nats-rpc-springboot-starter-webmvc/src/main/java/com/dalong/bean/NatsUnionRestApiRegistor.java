package com.dalong.bean;

import com.dalong.autoconfigure.config.ServiceMapping;
import com.dalong.autoconfigure.config.UnionMapping;
import com.dalong.handler.ServiceHandler;
import com.dalong.handler.UnionHandler;
import com.dalong.util.methodutils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class NatsUnionRestApiRegistor {
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private List<UnionHandler> serviceHandlers;

    public NatsUnionRestApiRegistor(RequestMappingHandlerMapping requestMappingHandlerMapping, List<UnionHandler> serviceHandlers) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.serviceHandlers = serviceHandlers;
    }

    public void registerRestApis() {
        serviceHandlers.forEach(serviceHandler -> {
            Class<?> targetClass =
                    AopProxyUtils.ultimateTargetClass(serviceHandler);
            Method[] methods = targetClass.getMethods();
            Arrays.stream(methods).forEach(method -> {
                if (method.isBridge()) {
                    return;
                }
                UnionMapping serviceMapping = method.getAnnotation(UnionMapping.class);
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
