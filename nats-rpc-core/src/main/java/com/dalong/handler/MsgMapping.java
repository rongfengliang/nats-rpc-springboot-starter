package com.dalong.handler;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MsgMapping {
    String name() default "";
    String[] path() default {};
    String[] method() default {};
    String version() default "";
}