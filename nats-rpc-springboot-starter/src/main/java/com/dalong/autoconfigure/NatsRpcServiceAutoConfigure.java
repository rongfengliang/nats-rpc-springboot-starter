package com.dalong.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dalong.autoconfigure.config.RpcServiceConfig;
import com.dalong.autoconfigure.service.RegisterBizService;
import com.dalong.autoconfigure.service.RegisterMessageService;
import com.dalong.autoconfigure.service.RegisterRpcService;
import com.dalong.handler.BizServiceHandler;
import com.dalong.handler.ServiceHandler;
import com.dalong.handler.SubMessageHandler;
import com.dalong.registry.BizServiceHandlerRegistry;
import com.dalong.registry.ServiceHandlerRegistry;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@ConditionalOnProperty(
   prefix = "rpcservice",
   name = "enabled",
   havingValue = "true"
)
@EnableConfigurationProperties(RpcServiceConfig.class)
public class NatsRpcServiceAutoConfigure {

    @Bean
    @ConditionalOnMissingBean(Connection.class)
    public Connection connection(RpcServiceConfig rpcServiceConfig) {
        try {
            Options options;
            if(rpcServiceConfig.getMsgHub() == null || rpcServiceConfig.getMsgHub().getUrl() == null) {
                throw new RuntimeException("MsgHub configuration is missing");
            }
            if((rpcServiceConfig.getMsgHub().getCreds()!=null) && !rpcServiceConfig.getMsgHub().getCreds().isEmpty()) {
                options = new Options.Builder()
                        .useDispatcherWithExecutor()
                        .server(rpcServiceConfig.getMsgHub().getUrl())
                        .authHandler(Nats.credentials(rpcServiceConfig.getMsgHub().getCreds()))
                        .connectionName(rpcServiceConfig.getRpcServiceName())
                        .reconnectWait(Duration.ofSeconds(2))
                        .maxReconnects(-1)
                        .connectionTimeout(Duration.ofSeconds(5))
                        .build();
            } else{
                options = new Options.Builder()
                        .useDispatcherWithExecutor()
                        .server(rpcServiceConfig.getMsgHub().getUrl())
                        .userInfo(rpcServiceConfig.getMsgHub().getUsername(),rpcServiceConfig.getMsgHub().getPassword())
                        .connectionName(rpcServiceConfig.getRpcServiceName())
                        .reconnectWait(Duration.ofSeconds(2))
                        .maxReconnects(-1)
                        .connectionTimeout(Duration.ofSeconds(5))
                        .build();
            }
            Connection connection = Nats.connect(options);
            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public ServiceHandlerRegistry serviceHandlerRegistry(){
        return new ServiceHandlerRegistry();
    }

    @Bean
    public BizServiceHandlerRegistry bizServiceHandlerRegistry(){
        return new BizServiceHandlerRegistry();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnBean(Connection.class)
    public RegisterRpcService registerRpcService(List<ServiceHandler> serviceHandlers, Connection connection, RpcServiceConfig config, ServiceHandlerRegistry serviceHandlerRegistry, ObjectMapper objectMapper){
        return new RegisterRpcService(serviceHandlers,connection,config,serviceHandlerRegistry,objectMapper);
    }

    @Bean(destroyMethod = "unSubscribeAll")
    @ConditionalOnBean(Connection.class)
    public RegisterMessageService registerMessageService(List<SubMessageHandler> serviceHandlers, Connection connection, RpcServiceConfig config, ServiceHandlerRegistry serviceHandlerRegistry, ObjectMapper objectMapper){
        return new RegisterMessageService(serviceHandlers,connection,config,serviceHandlerRegistry,objectMapper);
    }

    @Bean
    public RegisterBizService registerBizService(List<BizServiceHandler> bizServiceHandlers, BizServiceHandlerRegistry bizServiceHandlerRegistry){
        return new RegisterBizService(bizServiceHandlers,bizServiceHandlerRegistry);
    }
}
