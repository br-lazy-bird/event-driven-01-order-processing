package com.lazybird.common.messaging;

public class OrderMessagingConstants {

    // Exchange
    public static final String ORDERS_EXCHANGE = "orders.exchange";

    // Queues
    public static final String PROCESS_QUEUE = "orders.process.queue";
    public static final String DLQ_QUEUE = "orders.dlq.queue";

    // Routing Keys
    public static final String PROCESS_KEY = "order.process";
    public static final String DLQ_KEY = "order.dlq";

    private OrderMessagingConstants() {
    }
}
