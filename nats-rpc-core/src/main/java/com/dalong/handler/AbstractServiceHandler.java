package com.dalong.handler;

import com.dalong.helper.MessageHelper;
import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.api.Error;
import io.nats.client.impl.Headers;
import io.nats.service.ServiceMessage;

import java.lang.reflect.Method;

import static com.dalong.helper.MessageHelper.actionMethodExecute;

public abstract  class AbstractServiceHandler<T extends BaseMessage> implements  ServiceHandler<T>{
    public T messageConvert(byte[] data) {
        T baseMessage =  MessageHelper.castBaseMessage(data,this.getMessageType(),this.getObjectMapper());
        return  baseMessage;
    }
    public  abstract  Connection getConnection();
    public  abstract ObjectMapper getObjectMapper();
    @Override
    public  abstract Object defaultMessageHandler(T message,Headers headers);
    @Override
    public void onMessage(ServiceMessage smsg) {
        T baseMessage =  messageConvert(smsg.getData());
        Headers headers = smsg.getHeaders();
        Method actionMethod = actionMethod(baseMessage);
        boolean continueProcess = beforeHandle(actionMethod,baseMessage,headers);
        if(continueProcess) {
            Object result = actionMethodExecute(actionMethod, this, baseMessage, headers);
            try {
                if (headers != null) {
                    smsg.respond(getConnection(), getObjectMapper().writeValueAsBytes(result), headers);
                } else {
                    smsg.respond(getConnection(), getObjectMapper().writeValueAsBytes(result));
                }
                afterHandle(actionMethod,result, headers);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            smsg.respond(getConnection(),  Error.JsBadRequestErr);
        }
    }
}
