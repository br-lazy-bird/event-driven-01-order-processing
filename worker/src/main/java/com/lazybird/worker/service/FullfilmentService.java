package com.lazybird.worker.service;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lazybird.worker.exceptions.FulfillmentException;

@Service
public class FullfilmentService {

    private static final Logger log = LoggerFactory.getLogger(FullfilmentService.class);
    private final Random random = new Random();

    public void fulfillOrder(String orderid) {
        log.info("Attempting to fulfill order: {}", orderid);
        simulateProcessing();

        // 50% chance of failure
        if (random.nextBoolean()) {
            throw new FulfillmentException("Order fulfillment failed for:" + orderid);
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
