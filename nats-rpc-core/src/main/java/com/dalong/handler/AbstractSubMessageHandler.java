package com.dalong.handler;

import com.dalong.helper.MessageHelper;
import com.dalong.models.BaseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.Headers;

import java.lang.reflect.Method;

import static com.dalong.helper.MessageHelper.actionMethodExecute;


public  abstract  class AbstractSubMessageHandler<T extends BaseMessage> implements SubMessageHandler<T>{
    public T messageConvert(byte[] data) {
        T baseMessage =  MessageHelper.castBaseMessage(data,this.getMessageType(),this.getObjectMapper());
        return  baseMessage;
    }
    public  abstract Connection getConnection();
    public  abstract ObjectMapper getObjectMapper();
    @Override
    public  abstract Object defaultMessageHandler(T message, Headers headers);
    @Override
    public void onMessage(Message msg) throws InterruptedException {
        T baseMessage =  messageConvert(msg.getData());
        Method actionMethod = actionMethod(baseMessage);
        Headers headers = msg.getHeaders();
        if(beforeHandle(actionMethod,baseMessage,headers)) {
            Object result =  actionMethodExecute(actionMethod,this,baseMessage,headers);
            afterHandle(actionMethod,result,headers);
        }
    }
}
