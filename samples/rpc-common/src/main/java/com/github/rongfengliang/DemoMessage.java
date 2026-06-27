package com.github.rongfengliang;

import com.dalong.models.BaseMessage;
import lombok.Data;

@Data
public class DemoMessage extends BaseMessage {
    private String name;
    private Integer age;
}