package com.lazybird.worker.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.lazybird.common.messaging.OrderMessagingConstants;
import com.lazybird.worker.exceptions.FulfillmentException;
import com.lazybird.worker.model.Order.OrderStatus;

@Service
public class OrderProcessor {

    private final FulfillmentService fulfillmentService;
    private final OrderUpdateService orderUpdateService;
    private static final Logger logger = LoggerFactory.getLogger(OrderProcessor.class);

    public OrderProcessor(FulfillmentService fulfillmentService, OrderUpdateService orderUpdateService) {
        this.fulfillmentService = fulfillmentService;
        this.orderUpdateService = orderUpdateService;
    }

    @RabbitListener(queues = OrderMessagingConstants.PROCESS_QUEUE)
    public void process(String orderId) {
        try {
            fulfillmentService.fulfillOrder(orderId);
            orderUpdateService.changeOrderState(UUID.fromString(orderId), OrderStatus.COMPLETED);
            logger.info("Order id: {} COMPLETED", orderId);
        } catch (FulfillmentException e) {
            logger.error("Fulfillment failed for order: {}, sending to DLQ", orderId);
            throw e;
        }

    }
}
