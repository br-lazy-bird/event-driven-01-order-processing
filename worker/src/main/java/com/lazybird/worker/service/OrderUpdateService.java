package com.lazybird.worker.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lazybird.worker.model.Order;
import com.lazybird.worker.model.Order.OrderStatus;
import com.lazybird.worker.repository.OrderRepository;

@Service
public class OrderUpdateService {

    private final OrderRepository orderRepository;

    public OrderUpdateService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void changeOrderState(UUID orderId, OrderStatus status) {
        Order order = orderRepository.getReferenceById(orderId);
        order.setStatus(status);
        orderRepository.save(order);
    }

}
