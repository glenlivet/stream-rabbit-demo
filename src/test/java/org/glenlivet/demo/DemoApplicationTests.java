package org.glenlivet.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = DemoApplication.class)
public class DemoApplicationTests {

    @Autowired
    private Processor pipe;

    @Test
    public void testFailed() throws InterruptedException {
        pipe.output()
                .send(MessageBuilder.withPayload("fail")
                        .build());
        Thread.sleep(20000);
        assertEquals(DemoApplication.countMap.get("fail"), new Integer(3));
    }

    @Test
    public void testSuccessful() throws InterruptedException {
        pipe.output()
                .send(MessageBuilder.withPayload("success")
                        .build());
        Thread.sleep(20000);
        assertEquals(DemoApplication.countMap.get("success"), new Integer(1));
    }
}
