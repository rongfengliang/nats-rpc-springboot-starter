package com.dalong.autoconfigure.service;

import com.dalong.handler.BizServiceHandler;
import com.dalong.registry.BizServiceHandlerRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RegisterBizService {
    public RegisterBizService(List<BizServiceHandler> bizServiceHandlers, BizServiceHandlerRegistry bizServiceHandlerRegistry) {
        bizServiceHandlers.forEach(bizServiceHandler -> {
            bizServiceHandlerRegistry.register(bizServiceHandler);
            log.info("Registered BizServiceHandler for type: {}", bizServiceHandler.getMessageType().getSimpleName());
        });
    }
}
