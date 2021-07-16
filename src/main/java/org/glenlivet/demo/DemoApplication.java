package org.glenlivet.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableBinding({Processor.class})
public class DemoApplication {

	public static Logger LOG = LoggerFactory.getLogger(DemoApplication.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public static Map<String, Integer> countMap = new ConcurrentHashMap<>();

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@StreamListener(Processor.INPUT)
	@MessageRequeue
	public synchronized void countRetry(Message message, String name) {
		/**
		 * 每执行一次 根据name计数
		 */
		Integer count = countMap.get(name);
		if (count == null) {
			countMap.put(name, 1);
		} else {
			countMap.put(name, ++count);
		}
		/**
		 * 只有当name == "fail" 的时候报错。
		 */
		if ("fail".equals(name)) {
			throw new RuntimeException("failed.");
		}
	}

}
