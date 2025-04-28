package com.lhk.examplespringbootconsumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ExampleSpringbootConsumerApplicationTests {

    @Resource
    private ExampleConsumer exampleConsumer;

    @Test
    void consumer() {
        exampleConsumer.consumer();
    }

}
