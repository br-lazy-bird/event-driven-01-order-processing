package com.lazybird.orderprocessing.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lazybird.common.messaging.OrderMessagingConstants;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(OrderMessagingConstants.ORDERS_EXCHANGE, true, false);
    }

    @Bean
    public Queue ordersQueue() {
        return new Queue(OrderMessagingConstants.ORDERS_QUEUE, true);
    }

    @Bean
    public Binding ordersBinding(Queue ordersQueue, DirectExchange ordersExchange) {
        return BindingBuilder
                .bind(ordersQueue)
                .to(ordersExchange)
                .with(OrderMessagingConstants.ORDERS_ROUTING_KEY);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
