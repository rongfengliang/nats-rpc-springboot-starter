package com.dalong.bean;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class GlobalBinderConfig {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // 拒绝 Spring 自动绑定 Accept-Language
        binder.setDisallowedFields("acceptLanguage");
    }
}