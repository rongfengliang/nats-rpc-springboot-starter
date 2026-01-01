package com.dalong.registry;
import com.dalong.handler.BizServiceHandler;
import com.dalong.models.BaseMessage;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息处理器注册中心
 * 管理消息类型和对应的处理器映射关系
 */

@Slf4j
public class BizServiceHandlerRegistry {
    
    /**
     * 消息类型 -> 处理器的映射
     */
    private final Map<Class<? extends BaseMessage>, BizServiceHandler> handlers = new ConcurrentHashMap<>();
    
    /**
     * 消息类型名称 -> 消息类型的映射（用于反序列化时查找）
     */
    private final Map<String, Class<? extends BaseMessage>> messageTypes = new ConcurrentHashMap<>();

    /**
     * 注册消息处理器, 不注册子类型, 需要显示指定类型序列化
     * @param handler
     * @param <T>
     */
    public <T extends BaseMessage> void register(BizServiceHandler handler) {
        Class<T> messageType = handler.getMessageType();
        handlers.put(messageType, handler);
        log.info("Registered bizhandler for type: {}", messageType.getSimpleName());
    }

    public Map<Class<? extends BaseMessage>, BizServiceHandler> getHandlers() {
        return handlers;
    }

    public  Map<String, Class<? extends BaseMessage>> getMessageTypes() {
        return messageTypes;
    }
    
    /**
     * 获取消息处理器
     * 
     * @param messageType 消息类型
     * @return 消息处理器，如果未找到返回 null
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseMessage,R> BizServiceHandler<T> getHandler(Class<T> messageType) {
        return (BizServiceHandler<T>) handlers.get(messageType);
    }

    public Method getMessageActionHandlerMethod(BaseMessage message) {
        BizServiceHandler handler = getHandler(message.getClass());
        if (handler != null) {
            return handler.actionMethod(message);
        }
        return null;
    }
    
    /**
     * 根据消息类型名称获取消息类型
     * 
     * @param typeName 消息类型名称
     * @return 消息类型，如果未找到返回 null
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseMessage> Class<T> getMessageType(String typeName) {
        return (Class<T>) messageTypes.get(typeName);
    }
    
    /**
     * 检查是否有对应的处理器
     * 
     * @param messageType 消息类型
     * @return 是否存在处理器
     */
    public boolean hasHandler(Class<? extends BaseMessage> messageType) {
        return handlers.containsKey(messageType);
    }

}

