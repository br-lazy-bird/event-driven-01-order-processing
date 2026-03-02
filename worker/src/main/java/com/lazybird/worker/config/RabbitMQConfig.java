package com.lazybird.worker.config;

import com.lazybird.common.messaging.OrderMessagingConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue ordersQueue() {
        return new Queue(OrderMessagingConstants.ORDERS_QUEUE, true);
    }
}
