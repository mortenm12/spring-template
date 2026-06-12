package com.example.template.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ItemEventListener {

    private static final Logger log = LoggerFactory.getLogger(ItemEventListener.class);

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void onItemEvent(ItemEvent event) {
        log.info("Received item event: {}", event);
    }
}
