package com.dalong.handler;

import com.dalong.models.BaseMessage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SubMessageHandlerType {
    String typeValue();
    String version() default "1.0.0";
    String scope() default "global";
    String description() default "";
    String subjectName() default "";
    Class<? extends BaseMessage> messageClass();
}
