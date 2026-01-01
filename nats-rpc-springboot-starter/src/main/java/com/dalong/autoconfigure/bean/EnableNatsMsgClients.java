package com.dalong.autoconfigure.bean;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(NatsMsgClientsRegistrar.class)
@Documented
public @interface EnableNatsMsgClients {
    String[] basePackages() default {};
}