package com.dalong.autoconfigure.bean;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(NatsRpcClientsRegistrar.class)
@Documented
public @interface EnableNatsRpcClients {
    String[] basePackages() default {};
}