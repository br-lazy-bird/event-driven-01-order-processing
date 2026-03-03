package com.lazybird.orderprocessing.service;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.lazybird.common.messaging.OrderMessagingConstants;

@Service
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrder(UUID orderId) {
        rabbitTemplate.convertAndSend(
                OrderMessagingConstants.ORDERS_EXCHANGE,
                OrderMessagingConstants.PROCESS_KEY,
                orderId.toString());
    }

}
