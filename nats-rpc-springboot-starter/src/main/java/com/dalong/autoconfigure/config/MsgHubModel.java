package com.dalong.autoconfigure.config;

import lombok.Data;

@Data
public class MsgHubModel {
    private String url;
    private String username;
    private String password;
    private String creds;
}
