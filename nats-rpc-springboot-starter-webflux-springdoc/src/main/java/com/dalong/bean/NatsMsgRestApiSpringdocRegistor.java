package com.dalong.bean;

import com.dalong.autoconfigure.config.MsgMapping;
import com.dalong.handler.SubMessageHandler;
import org.springdoc.core.utils.SpringDocUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class NatsMsgRestApiSpringdocRegistor {
    private List<SubMessageHandler> subMessageHandlers;

    public NatsMsgRestApiSpringdocRegistor(List<SubMessageHandler> subMessageHandlers) {
        this.subMessageHandlers = subMessageHandlers;
    }

    public void registerRestApis() {
        subMessageHandlers.forEach(subMessageHandler -> {
            Method[] methods = subMessageHandler.getClass().getDeclaredMethods();
            Arrays.stream(methods).forEach(method -> {
                if (method.isBridge()) {
                    return;
                }
                MsgMapping msgMapping = method.getAnnotation(MsgMapping.class);
                if (msgMapping == null) {
                    return;
                }
                SpringDocUtils.getConfig().addRestControllers(subMessageHandler.getClass());
            });
        });
    }

}
