package com.dalong.bean;

import com.dalong.handler.ServiceHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Configuration
@ConditionalOnBean(RequestMappingHandlerMapping.class)
public class NatsRpcApiConnfig {
    @Bean(initMethod = "registerRestApis")
    public NatsRpcRestApiRegistor natsRpcRestApiRegistrar(RequestMappingHandlerMapping requestMappingHandlerMapping, List<ServiceHandler> serviceHandlers) {
        return new NatsRpcRestApiRegistor(requestMappingHandlerMapping, serviceHandlers);
    }

    @Bean
    public GlobalBinderConfig globalBinderConfig() {
        return new GlobalBinderConfig();
    }
}
