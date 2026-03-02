package com.lazybird.orderprocessing.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.lazybird.orderprocessing.dto.BatchOrderResponse;
import com.lazybird.orderprocessing.dto.OrderResponse;
import com.lazybird.orderprocessing.mapper.OrderMapper;
import com.lazybird.orderprocessing.model.Order;
import com.lazybird.orderprocessing.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitMQPublisher rabbitMQPublisher;
    private final QueueManagementService queueManagementService;

    public OrderService(OrderRepository orderRepository, RabbitMQPublisher rabbitMQPublisher,
            QueueManagementService queueManagementService) {
        this.orderRepository = orderRepository;
        this.rabbitMQPublisher = rabbitMQPublisher;
        this.queueManagementService = queueManagementService;
    }

    public BatchOrderResponse createAndPublishOrders() {
        List<String> products = List.of("Laptop", "Headphones", "Keyboard", "Mouse", "Monitor");
        List<Order> orders = products.stream().map(product -> {
            Order order = new Order();
            order.setProduct(product);
            order.setQuantity(ThreadLocalRandom.current().nextInt(1, 11));
            order.setStatus(Order.OrderStatus.PENDING);
            return order;
        }).toList();

        orderRepository.saveAll(orders);

        List<UUID> orderIds = orders.stream().map(Order::getId).toList();
        orderIds.forEach(id -> rabbitMQPublisher.publishOrder(id.toString()));

        return new BatchOrderResponse(orderIds);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderMapper::toDTO)
                .toList();
    }

    public void clearDatabase() {
        orderRepository.deleteAll();
    }

    public void purgeQueue() {
        queueManagementService.purgeOrdersQueue();
    }

}
