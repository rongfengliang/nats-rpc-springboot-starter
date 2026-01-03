package com.dalong.models;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.OptBoolean;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, requireTypeIdForSubtypes = OptBoolean.FALSE, property = "type", visible = true)
@Data
public class BaseMessage {
    private String type;
    private String action;
}
