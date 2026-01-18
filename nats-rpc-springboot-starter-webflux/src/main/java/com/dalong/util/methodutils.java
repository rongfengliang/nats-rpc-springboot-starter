package com.dalong.util;

import org.springframework.web.bind.annotation.RequestMethod;

public class methodutils {
    public static RequestMethod str2RequestMethod(String method) {
        switch (method.toUpperCase()) {
            case "GET":
                return RequestMethod.GET;
            case "POST":
                return RequestMethod.POST;
            case "PUT":
                return RequestMethod.PUT;
            case "DELETE":
                return RequestMethod.DELETE;
            case "PATCH":
                return RequestMethod.PATCH;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }
}
