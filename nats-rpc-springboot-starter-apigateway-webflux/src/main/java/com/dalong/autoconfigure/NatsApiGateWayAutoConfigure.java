package com.dalong.autoconfigure;

import com.dalong.apihandlers.NatsMsgServiceHandler;
import com.dalong.apihandlers.NatsRpcServiceHandler;
import com.dalong.apihandlers.NatsUnionServiceHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(
        prefix = "rpcservice",
        name = "enabled",
        havingValue = "true"
)
@Import(value = {
        NatsMsgServiceHandler.class,
        NatsRpcServiceHandler.class,
        NatsUnionServiceHandler.class
})
public class NatsApiGateWayAutoConfigure {
}
