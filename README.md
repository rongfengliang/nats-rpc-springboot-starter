# nats micro service rpc framework

## Overview

nats micro service rpc framework is a lightweight and high-performance microservice framework based on NATS

## Features

- High performance: Built on NATS, it provides low-latency communication between microservices  
- Easy to use: Simple API for defining and calling microservices  
- Scalable: Easily scale your microservices as needed
- Flexible: Supports micro service styles and msg styles
- Spring boot integration: Seamlessly integrates with Spring Boot applications
- Fegin style rpc call support: Supports Fegin style rpc calls for easy integration with existing services
- Rest api export support: Easily expose your microservices as RESTful APIs


## usage

### 1. Add dependencies

pom.xml 
```xml
<dependency>
    <groupId>com.dalong</groupId>
    <artifactId>nats-rpc-springboot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. Configure NATS server address

application.yml
```yaml
rpcservice:
  enabled: true
  msghub:
    url: nats://localhost:14222
    username: xxxxx
    password: xxxxxxx
  rpc-service-name: "xxxxx"
  rpcservice-prefix: "xxxxxx"
```

* message class

````java
@Data
public class DemoMessage  extends BaseMessage {
    private  Config data;
}
````

### 3. Implement rpc microservice handler

*  rpc microservice handler

````java
@ServiceHandlerType(
        typeValue = "xxxxx",
        version = "1.0.0",
        scope = "xxxxx",
        description = "xxxxx",
        endpointName = "xxxxxx",
        messageClass = DemoMessage.class)
@Component
public class DemoServiceHandler extends AbstractServiceHandler<DemoMessage> {

    private final  Connection connection;
    private final ObjectMapper objectMapper;
    public  DemoServiceHandler(ObjectMapper objectMapper, Connection connection) {
        this.objectMapper = objectMapper;
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    public Object defaultMessageHandler(DemoMessage message, Headers headers) {
        // default handler
        return List.of(message, message,message);
    }

    public List<String> getRoles(DemoMessage message, Headers headers) {
        return List.of("admin","user","guest");
    }
    public List<String> delteRoles(DemoMessage message, Headers headers) {
        return List.of("role1 deleted","role2 deleted");
    }
}
````

* client rpc call

feign style api interface

```java
import com.sun.net.httpserver.Headers;

@RpcClient(
        serviceName = "xxxx",
        servicePrefix = "xxxx",
        serviceEndpoint = "xxxx"
)
public interface AuthApi {
    List<String> getRoles(Message message);

    List<String> getRoles(DemoMessage message, Headers headers);

    List<String> getRoles(String prefix, Message message);

    List<String> getRoles(String prefix, Message message, Headers headers);
}
```
call rpc api

```java

// need ObjectMapper and NATS Connection instance

 AuthApi serviceClient = RpcServiceClient.builder().objectMapper(objectMapper)
                .connection(connection)
                .build().target(AuthApi.class);
```
### 4. msg style rpc call

* msg server handler

```java
@SubMessageHandlerType(
        subjectName = "xxxxx",
        typeValue = "xxxxx",
        version = "1.0.0",
        scope = "xxxx",
        description = "xxxxx",
        messageClass = DemoMessage.class
)
@Component
public class DemoSubMessageHandler extends AbstractSubMessageHandler<DemoMessage> {

    private final  Connection connection;
    private final ObjectMapper objectMapper;
    public  DemoSubMessageHandler(ObjectMapper objectMapper, Connection connection) {
        this.objectMapper = objectMapper;
        this.connection = connection;
    }
    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    public Object defaultMessageHandler(DemoMessage message, Headers headers) {
        return message;
    }
    public List<String> authMsg(DemoMessage message, Headers headers) {
        return List.of("auth1","auth2","auth3");
    }
}
```

* msg client call

feign style api interface 
 
```java
@MsgClient(
        msgSubject = "xxxx",
        serviceName = "xxxx",
        servicePrefix = "xxxx"
)
public interface MsgApi {
   void authMsg(Message message);
   void authMsg(Message message,Headers headers);
   void authMsg(String prefix, Message message);
   void authMsg(String prefix, Message message, Headers headers);
}
```
call msg api

```java
// need ObjectMapper and NATS Connection instance

MsgApi serviceClient = RpcServiceClient.builder().objectMapper(objectMapper)
                .connection(connection)
                .build().targetMsg(MsgApi.class);
```

### 5. spring boot EnbleFeginClients

* add @EnableRpcClients annotation to spring boot main class

```java
@SpringBootApplication
@EnableNatsMsgClients(basePackages = {
        "xxxxx"
})
public class RpcDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcDemoApplication.class, args);
    }
}
```

### 6. export rest api

* add dependency

```xml
<dependency>
    <groupId>com.dalong</groupId>
    <artifactId>nats-rpc-springboot-starter-webmvc</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

* handler add method annotation 

```java
public  Object echoDemo(DemoMessage demoMessage, Headers headers) {
    return demoMessage;
}

@ResponseBody
@ServiceMapping(path = {"/appdemo"},method = {"POST"})
public Object echoDemo(@RequestBody DemoMessage demoMessage, @RequestHeader(name = "token", required = false) String token){
    return echoDemo(demoMessage, new Headers());
}
```
### 7. rest api spring mvc support

extend RestApiAbstractServiceHandler

```java
public class DemoNatsServiceHandler extends RestApiAbstractServiceHandler<DemoMessage> {

    @ResponseBody
    @ServiceMapping(
            name = "defaultRestApiHandler",
            path = {"/api/default/rest/{servicename}/{prefix}/{serviceendpoint}"},
            method = {"POST"},
            version = "1.0.0"
    )
    @Override
    public Object defaultRestApiHandler(@PathVariable(name = "servicename") String serviceName,
                                        @PathVariable(name = "prefix") String prefix,
                                        @PathVariable(name = "serviceendpoint") String serviceEndpoint,
                                        @RequestBody   DemoMessage demoMessage,
                                        @Parameter(hidden = true) @RequestHeader  HttpHeaders httpHeaders) {
        return super.defaultRestApiHandler(serviceName,prefix,serviceEndpoint, demoMessage, httpHeaders);
    }
```


### 8.  msg rest api spring mvc support

extend MsgApiAbstractMsgHandler

```java
public class DemoNatsServiceHandlerV2 extends MsgApiAbstractMsgHandler<DemoMessage> {

    @ResponseBody
    @MsgMapping(
            name = "defaultRestApiHandler",
            path = {"/api/default/rest/{servicename}/{prefix}/{serviceendpoint}"},
            method = {"POST"},
            version = "1.0.0"
    )
    @Override
    public Object defaultMsgApiHandler(@PathVariable(name = "servicename") String serviceName,
                                        @PathVariable(name = "prefix") String prefix,
                                        @PathVariable(name = "serviceendpoint") String serviceEndpoint,
                                        @RequestBody   DemoMessage demoMessage,
                                        @Parameter(hidden = true) @RequestHeader  HttpHeaders httpHeaders) {
        return super.defaultMsgApiHandler(serviceName,prefix,serviceEndpoint, demoMessage, httpHeaders);
    }
```

### 9. rest api springdoc support

```java
<dependency>
   <groupId>com.dalong</groupId>
   <artifactId>nats-rpc-springboot-starter-webmvc-springdoc</artifactId>
   <version>1.0-SNAPSHOT</version>
</dependency>
```