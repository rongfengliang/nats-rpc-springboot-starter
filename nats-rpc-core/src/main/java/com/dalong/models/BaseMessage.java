package com.dalong.models;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type",visible = true)
@Data
public class BaseMessage {
    private  String type;
    private  String action;
}
