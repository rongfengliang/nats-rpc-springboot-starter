package com.dalong.autoconfigure.config;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceMapping {
    String name() default "";

    String[] path() default {};

    String[] method() default {};

    String version() default "";
}