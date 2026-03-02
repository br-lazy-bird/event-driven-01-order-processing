package com.lazybird.worker.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.lazybird.common.messaging.OrderMessagingConstants;

@Service
public class OrderProcessor {

    @RabbitListener(queues = OrderMessagingConstants.ORDERS_QUEUE)
    public void process(String orderId) {
        System.out.println("WORKER: ORDER ID: " + orderId);
    }

}
