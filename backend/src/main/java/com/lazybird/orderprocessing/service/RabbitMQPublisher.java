package com.lazybird.orderprocessing.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.lazybird.common.messaging.OrderMessagingConstants;

@Service
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrder(String orderId) {
        rabbitTemplate.convertAndSend(
                OrderMessagingConstants.ORDERS_EXCHANGE,
                OrderMessagingConstants.ORDERS_ROUTING_KEY,
                orderId);
    }

}
