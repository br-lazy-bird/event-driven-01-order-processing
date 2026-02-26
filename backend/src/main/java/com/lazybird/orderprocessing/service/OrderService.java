package com.lazybird.orderprocessing.service;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.lazybird.common.model.Order;
import com.lazybird.common.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private RabbitMQPublisher rabbitMQPublisher;

    public OrderService(OrderRepository orderRepository, RabbitMQPublisher rabbitMQPublisher) {
        this.orderRepository = orderRepository;
        this.rabbitMQPublisher = rabbitMQPublisher;
    }

    public List<Order> createOrders() {
        List<String> products = List.of("Laptop", "Headphones", "Keyboard", "Mouse", "Monitor");
        Random random = new Random();

        List<Order> orders = products.stream().map(product -> {
            Order order = new Order();
            order.setProduct(product);
            order.setQuantity(random.nextInt(10) + 1);
            order.setStatus(Order.OrderStatus.PENDING);
            return order;
        }).toList();

        return orderRepository.saveAll(orders);
    }

    
    
    public void publishOrders(List<String> orderIds) {
        orderIds.forEach(rabbitMQPublisher::publishOrder);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void clearDatabase() {
        orderRepository.deleteAll();
    }

    
}
