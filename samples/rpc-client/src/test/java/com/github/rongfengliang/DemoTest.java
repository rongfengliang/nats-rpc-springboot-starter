package com.github.rongfengliang;


import io.nats.client.impl.Headers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest(classes = RpcClientApp.class)
@RunWith(SpringRunner.class)
public class DemoTest {

    @Autowired
    DemoApi demoApi;

    @Test
    public void callService() {
        DemoMessage message = new DemoMessage();
        message.setName("Alice");
        message.setAge(30);

        // Call the default handler
        demoApi.defaultMessageHandler(message, new Headers().add("type", "demo"));
        //System.out.println(greeting);  // Output: Hello, Alice
        System.out.println("defaultMessageHandler called, no awaiting.");
        // Call the getRoles action
        List<String> roles = demoApi.getRoles(message);
        System.out.println(roles);  // Output: [admin, user, guest]
    }
}
