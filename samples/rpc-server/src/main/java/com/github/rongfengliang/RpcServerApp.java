package com.github.rongfengliang;

import com.dalong.autoconfigure.bean.EnableNatsMsgClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RpcServerApp {
    public static void main(String[] args) {
        SpringApplication.run(RpcServerApp.class, args);
    }
}
