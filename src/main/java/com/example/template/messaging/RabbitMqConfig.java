package com.example.template.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "items.exchange";
    public static final String QUEUE = "items.queue";
    public static final String ROUTING_KEY_PATTERN = "item.#";

    @Bean
    public TopicExchange itemsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue itemsQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding itemsBinding(Queue itemsQueue, TopicExchange itemsExchange) {
        return BindingBuilder.bind(itemsQueue).to(itemsExchange).with(ROUTING_KEY_PATTERN);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
