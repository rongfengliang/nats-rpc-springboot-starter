package com.dalong.bean;

import com.dalong.handler.ServiceHandler;
import com.dalong.handler.SubMessageHandler;
import com.dalong.handler.UnionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Configuration
@ConditionalOnBean(RequestMappingHandlerMapping.class)
public class NatsRpcApiOpenApiConnfig {
    @Bean(initMethod = "registerRestApis")
    public NatsRpcRestApiSpringdocRegistor natsRpcRestApiSpringdocRegistor(List<ServiceHandler> serviceHandlers) {
        return new NatsRpcRestApiSpringdocRegistor(serviceHandlers);
    }

    @Bean(initMethod = "registerRestApis")
    public NatsMsgRestApiSpringdocRegistor natsMsgRestApiSpringdocRegistor(List<SubMessageHandler> subMessageHandlers) {
        return new NatsMsgRestApiSpringdocRegistor(subMessageHandlers);
    }

    @Bean(initMethod = "registerRestApis")
    public NatsUnionRestApiSpringdocRegistor natsUnionRestApiSpringdocRegistor(List<UnionHandler> unionHandlers) {
        return new NatsUnionRestApiSpringdocRegistor(unionHandlers);
    }
}

