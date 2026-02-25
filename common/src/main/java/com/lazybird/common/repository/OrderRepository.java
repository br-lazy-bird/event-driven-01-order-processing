package com.lazybird.common.repository;

import com.lazybird.common.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find all orders sorted by creation date in descending order (newest first)
     */
    List<Order> findAllByOrderByCreatedAtDesc();
}
