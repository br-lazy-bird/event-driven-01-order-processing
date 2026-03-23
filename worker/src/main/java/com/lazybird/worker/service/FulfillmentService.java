package com.lazybird.worker.service;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lazybird.worker.exceptions.FulfillmentException;

@Service
public class FulfillmentService {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentService.class);
    private static final Random random = new Random();

    public void fulfillOrder(String orderId) {
        log.info("Attempting to fulfill order: {}", orderId);
        simulateProcessing();

        // 50% chance of failure
        if (random.nextBoolean()) {
            throw new FulfillmentException("Order fulfillment failed for:" + orderId);
        }
    }

    public void simulateProcessing() {
        try {
            Thread.sleep(100 + random.nextInt(400));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
