package com.lazybird.orderprocessing.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.lazybird.common.model.Order;
import com.lazybird.common.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitMQPublisher rabbitMQPublisher;

    public OrderService(OrderRepository orderRepository, RabbitMQPublisher rabbitMQPublisher) {
        this.orderRepository = orderRepository;
        this.rabbitMQPublisher = rabbitMQPublisher;
    }

    public List<String> createAndPublishOrders() {
        List<String> products = List.of("Laptop", "Headphones", "Keyboard", "Mouse", "Monitor");
        List<Order> orders = products.stream().map(product -> {
            Order order = new Order();
            order.setProduct(product);
            order.setQuantity(ThreadLocalRandom.current().nextInt(1, 11));
            order.setStatus(Order.OrderStatus.PENDING);
            return order;
        }).toList();

        orderRepository.saveAll(orders);

        List<String> orderIds = orders.stream()
                  .map(Order::getId)
                  .map(UUID::toString)
                  .toList();
                  
        orderIds.forEach(rabbitMQPublisher::publishOrder);

        return orderIds;
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void clearDatabase() {
        orderRepository.deleteAll();
    }

    
}
