package com.github.rongfengliang;

import com.dalong.client.RpcServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.List;

@Configuration
public class DemoClientConfig {

    @Bean
    DemoApiClient demoApi(ObjectMapper objectMapper, Connection connection) {
        System.out.println("DemoClientConfig.demoApi");
        return RpcServiceClient.builder()
                .objectMapper(objectMapper)
                .connection(connection)
                .build()
                .target(DemoApiClient.class);
    }
}