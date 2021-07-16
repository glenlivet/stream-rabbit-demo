package org.glenlivet.demo;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 监听消息接收方法。默认重发3次。监听方法必须要同时注解@StreamListener和@MessageRequeue, 并且第一个参数必须是Message message.
 */
@Aspect
@Component
public class MessageListenerAspect {

    static Logger LOG = LoggerFactory.getLogger(MessageListenerAspect.class);


    @Pointcut("@annotation(org.glenlivet.demo.MessageRequeue) " +
            "and @annotation(org.springframework.cloud.stream.annotation.StreamListener)")
    private void messageRequeued(){}

    @Pointcut("args(message,..)")
    private void firstMessageParam(Message message) {}

    @Around("messageRequeued() and firstMessageParam(message)")
    public Object handleRetry(ProceedingJoinPoint joinPoint, Message message) throws Throwable {

        Object retVal;
        try {
            retVal = joinPoint.proceed();
            return retVal;
        } catch (Exception ex) {
            List death = message.getHeaders().get("x-death", ArrayList.class);
            if (death != null && ((Map) death.get(0)).get("count").equals(2L)) {
                LOG.error("消息接收失败！");
                LOG.error("Headers: " + message.getHeaders().toString());
                LOG.error("Payload:" + message.getPayload().toString());
                throw new ImmediateAcknowledgeAmqpException(ex);
            }
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

}
