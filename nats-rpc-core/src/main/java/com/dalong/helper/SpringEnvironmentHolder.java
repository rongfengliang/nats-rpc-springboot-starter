package com.dalong.helper;


import org.springframework.core.env.Environment;


public class SpringEnvironmentHolder {
    private static Environment environment;
    public SpringEnvironmentHolder(Environment environment) {
        SpringEnvironmentHolder.environment = environment;
    }


    public static String resolvePlaceholders(String key) {
        return environment.resolvePlaceholders(key);
    }
}