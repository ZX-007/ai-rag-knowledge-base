package com.lcx.app;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    void contextLoads() {
        System.out.println(chatClientBuilder);
    }

}
