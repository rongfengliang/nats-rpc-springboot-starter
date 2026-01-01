package com.dalong.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dalong.models.BaseMessage;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Duration;

public class NatsMsgInvocationHandler implements InvocationHandler {
    private final Connection nats;
    private final ObjectMapper objectMapper;
    private Duration defaultTimeout = Duration.ofSeconds(120);
    public NatsMsgInvocationHandler(Connection nats, ObjectMapper objectMapper) {
        this.nats = nats;
        this.objectMapper = objectMapper;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        Class<?> clazz = method.getDeclaringClass();
        MsgClient msgClient = clazz.getAnnotation(MsgClient.class);
        String serviceMsgSubjectFormat = "%s.msg.%s.%s";
        String subject = "";
        byte[] req = null;
        Headers headers = null;
        if(args.length==3 && (args[0] instanceof String)){
            subject = String.format(
                    serviceMsgSubjectFormat,
                    msgClient.serviceName(),
                    (String)args[0],
                    msgClient.msgSubject()
            );
            BaseMessage msg = (BaseMessage) args[1];
            // 每次直接覆盖action,使用方法名作为action,注意会覆盖掉之前的action值, 造成BaseMessage的action字段信息不太正确,此问题只存在于rpc调用场景
            // 理想情况，应该使用深拷贝来避免修改原始对象，但为了性能考虑，这里直接修改原始对象
            msg.setAction(method.getName());
            req = objectMapper.writeValueAsBytes(msg);
            headers = (Headers) args[2];
        }
        if (args.length==2 && (args[0] instanceof String)){
            subject = String.format(
                    serviceMsgSubjectFormat,
                    msgClient.serviceName(),
                    (String)args[0],
                    msgClient.msgSubject()
            );
            BaseMessage msg = (BaseMessage) args[1];
            // 每次直接覆盖action,使用方法名作为action,注意会覆盖掉之前的action值, 造成BaseMessage的action字段信息不太正确,此问题只存在于rpc调用场景
            // 理想情况，应该使用深拷贝来避免修改原始对象，但为了性能考虑，这里直接修改原始对象
            msg.setAction(method.getName());
            req = objectMapper.writeValueAsBytes(msg);
        }

        if (args.length==2 && (args[0] instanceof String)==false){
            subject = String.format(
                    serviceMsgSubjectFormat,
                    msgClient.serviceName(),
                    msgClient.servicePrefix(),
                    msgClient.msgSubject()
            );
            BaseMessage msg = (BaseMessage) args[0];
            // 每次直接覆盖action,使用方法名作为action,注意会覆盖掉之前的action值, 造成BaseMessage的action字段信息不太正确,此问题只存在于rpc调用场景
            // 理想情况，应该使用深拷贝来避免修改原始对象，但为了性能考虑，这里直接修改原始对象
            msg.setAction(method.getName());
            req = objectMapper.writeValueAsBytes(msg);
            headers = (Headers) args[1];
        }
        if(args.length==1) {
            subject = String.format(
                    serviceMsgSubjectFormat,
                    msgClient.serviceName(),
                    msgClient.servicePrefix(),
                    msgClient.msgSubject()

            );
            BaseMessage msg = (BaseMessage) args[0];
            // 每次直接覆盖action,使用方法名作为action,注意会覆盖掉之前的action值, 造成BaseMessage的action字段信息不太正确,此问题只存在于rpc调用场景
            // 理想情况，应该使用深拷贝来避免修改原始对象，但为了性能考虑，这里直接修改原始对象
            msg.setAction(method.getName());
            req = objectMapper.writeValueAsBytes(msg);
        }

        Type returnType = method.getGenericReturnType();
        if (returnType == Void.TYPE) {
            if(headers != null){
                nats.publish(subject, headers, req);
            }else{
                nats.publish(subject, req);
            }
            return null;
        }
        if(returnType != Void.TYPE) {
            throw  new RuntimeException("MsgClient methods must have void return type");
        }
        return  null;
    }
}
