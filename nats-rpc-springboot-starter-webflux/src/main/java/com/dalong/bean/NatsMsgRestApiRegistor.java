package com.dalong.bean;

import com.dalong.autoconfigure.config.MsgMapping;
import com.dalong.handler.SubMessageHandler;
import com.dalong.util.methodutils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class NatsMsgRestApiRegistor {
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private List<SubMessageHandler> subMessageHandlers;

    public NatsMsgRestApiRegistor(RequestMappingHandlerMapping requestMappingHandlerMapping, List<SubMessageHandler> subMessageHandlers) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.subMessageHandlers = subMessageHandlers;
    }

    public void registerMsgApis() {
        subMessageHandlers.forEach(subMessageHandler -> {
            Class<?> targetClass =
                    AopProxyUtils.ultimateTargetClass(subMessageHandler);
            Method[] methods = targetClass.getMethods();
            Arrays.stream(methods).forEach(method -> {
                if (method.isBridge()) {
                    return;
                }
                MsgMapping msgMapping = method.getAnnotation(MsgMapping.class);
                if (msgMapping == null) {
                    return;
                }
                if (method.getDeclaringClass() != targetClass) {
                    return;
                }
                RequestMappingInfo mapping = RequestMappingInfo
                        .paths(msgMapping.path())
                        .methods(Arrays.stream(msgMapping.method()).map(item -> methodutils.str2RequestMethod(item)).toArray(RequestMethod[]::new))
                        .build();
                Method handlerMethod = method;
                requestMappingHandlerMapping.registerMapping(mapping, subMessageHandler, handlerMethod);
            });
        });
    }

}
