package com.lazybird.orderprocessing.mapper;

import com.lazybird.orderprocessing.dto.OrderResponse;
import com.lazybird.orderprocessing.model.Order;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toDTO(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getProduct(),
                order.getQuantity(),
                order.getStatus().name(),
                order.getFailureReason(),
                order.getCreatedAt(),
                order.getUpdatedAt());

    }

}
