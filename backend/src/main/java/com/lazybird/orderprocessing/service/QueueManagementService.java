package com.lazybird.orderprocessing.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.lazybird.common.messaging.OrderMessagingConstants;

@Service
public class QueueManagementService {

    private final RabbitTemplate rabbitTemplate;

    public QueueManagementService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void purgeOrdersQueue() {
        rabbitTemplate.execute(channel -> {
            channel.queuePurge(OrderMessagingConstants.PROCESS_QUEUE);
            return null;
        });
    }

}
