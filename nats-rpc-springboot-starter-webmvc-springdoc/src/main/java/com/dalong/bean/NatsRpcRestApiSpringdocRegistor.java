package com.dalong.bean;

import com.dalong.autoconfigure.config.ServiceMapping;
import com.dalong.handler.ServiceHandler;
import com.dalong.util.methodutils;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class NatsRpcRestApiSpringdocRegistor {
    private List<ServiceHandler> serviceHandlers;

    public NatsRpcRestApiSpringdocRegistor( List<ServiceHandler> serviceHandlers) {
        this.serviceHandlers = serviceHandlers;
    }

    public void registerRestApis() {
        serviceHandlers.forEach(serviceHandler -> {
            Method[] methods = serviceHandler.getClass().getDeclaredMethods();
            Arrays.stream(methods).forEach(method -> {
                if(method.isBridge()){
                    return;
                }
                ServiceMapping serviceMapping = method.getAnnotation(ServiceMapping.class);
                if (serviceMapping == null) {
                    return;
                }
                SpringDocUtils.getConfig().addRestControllers(serviceHandler.getClass());
            });
        });
    }

}
