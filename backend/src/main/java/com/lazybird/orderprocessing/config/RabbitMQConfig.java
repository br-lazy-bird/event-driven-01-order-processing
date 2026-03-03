package com.lazybird.orderprocessing.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lazybird.common.messaging.OrderMessagingConstants;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(OrderMessagingConstants.ORDERS_EXCHANGE, true, false);
    }
}
