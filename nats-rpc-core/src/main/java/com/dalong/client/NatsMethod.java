package com.dalong.client;

import lombok.Data;

@Data
public class NatsMethod {
    private String serviceName;
    private String serviceEndpoint;
    private String servicePrefix = "global";
    private String action;
}
