package com.dalong.bean;

import com.dalong.autoconfigure.config.UnionMapping;
import com.dalong.handler.UnionHandler;
import org.springdoc.core.utils.SpringDocUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class NatsUnionRestApiSpringdocRegistor {
    private List<UnionHandler> serviceHandlers;

    public NatsUnionRestApiSpringdocRegistor(List<UnionHandler> serviceHandlers) {
        this.serviceHandlers = serviceHandlers;
    }

    public void registerRestApis() {
        serviceHandlers.forEach(serviceHandler -> {
            Method[] methods = serviceHandler.getClass().getDeclaredMethods();
            Arrays.stream(methods).forEach(method -> {
                if (method.isBridge()) {
                    return;
                }
                UnionMapping serviceMapping = method.getAnnotation(UnionMapping.class);
                if (serviceMapping == null) {
                    return;
                }
                SpringDocUtils.getConfig().addRestControllers(serviceHandler.getClass());
            });
        });
    }

}
