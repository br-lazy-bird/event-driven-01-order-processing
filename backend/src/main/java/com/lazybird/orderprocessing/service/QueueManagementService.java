package com.lazybird.orderprocessing.service;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

import com.lazybird.common.messaging.OrderMessagingConstants;

@Service
public class QueueManagementService {

    private final RabbitAdmin rabbitAdmin;

    public QueueManagementService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public void purgeOrdersQueue() {
        rabbitAdmin.purgeQueue(OrderMessagingConstants.ORDERS_QUEUE);
    }

}
