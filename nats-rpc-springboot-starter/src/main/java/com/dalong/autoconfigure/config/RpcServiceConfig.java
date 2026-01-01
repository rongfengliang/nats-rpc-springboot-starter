package com.dalong.autoconfigure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rpcservice")
@Data
public class RpcServiceConfig {
    private String rpcServiceName;
    private String rpcServiceVersion = "1.0.0";
    private String rpcServiceDescription = "NATS RPC Service";
    private int rpcServiceTimeout = 60000;
    private boolean enabled;
    private MsgHubModel msgHub;
    private String rpcServicePrefix;
}
