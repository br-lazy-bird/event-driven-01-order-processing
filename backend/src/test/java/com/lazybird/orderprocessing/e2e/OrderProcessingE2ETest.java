package com.lazybird.orderprocessing.e2e;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for order processing system.
 * Tests the complete flow: Backend -> RabbitMQ -> Worker -> Database
 *
 * This test verifies the bug: orders that fail fulfillment go to DLQ and remain PENDING forever.
 */
public class OrderProcessingE2ETest {

    private static final String API_URL = System.getenv().getOrDefault("API_URL", "http://test-backend:8000");
    private static final int PROCESSING_DELAY_SECONDS = 5;

    @Test
    public void testOrderProcessingWithDLQBug() throws InterruptedException {
        RestClient restClient = RestClient.create();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("Starting E2E Test: Order Processing with DLQ Bug");
        System.out.println("=".repeat(70));

        // Step 1: POST /api/orders/batch to create and publish orders
        System.out.println("\n[1] Creating batch orders...");
        String batchResponse = restClient.post()
                .uri(API_URL + "/api/orders/batch")
                .retrieve()
                .body(String.class);

        assertNotNull(batchResponse, "Batch order response should not be null");
        System.out.println("SUCCESS: Batch orders created");

        // Step 2: Wait for processing
        System.out.println("\n[2] Waiting " + PROCESSING_DELAY_SECONDS + " seconds for order processing...");
        Thread.sleep(PROCESSING_DELAY_SECONDS * 1000);
        System.out.println("SUCCESS: Processing time elapsed");

        // Step 3: GET /api/orders to check results
        System.out.println("\n[3] Fetching all orders...");
        List<Map<String, Object>> orders = restClient.get()
                .uri(API_URL + "/api/orders")
                .retrieve()
                .body(List.class);

        assertNotNull(orders, "Orders list should not be null");
        System.out.println("SUCCESS: Retrieved " + orders.size() + " orders");

        // Step 4: Verify order statuses
        System.out.println("\n[4] Verifying order statuses...");

        long completedCount = orders.stream()
                .filter(order -> "COMPLETED".equals(order.get("status")))
                .count();

        long pendingCount = orders.stream()
                .filter(order -> "PENDING".equals(order.get("status")))
                .count();

        long failedCount = orders.stream()
                .filter(order -> "FAILED".equals(order.get("status")))
                .count();

        System.out.println("  COMPLETED orders: " + completedCount);
        System.out.println("  PENDING orders:   " + pendingCount + " (stuck in DLQ - the bug!)");
        System.out.println("  FAILED orders:    " + failedCount);

        // Assertions
        assertTrue(completedCount >= 1, "Should have at least 1 COMPLETED order");
        assertTrue(pendingCount >= 1, "Should have at least 1 PENDING order (the bug!)");
        assertEquals(0, failedCount, "Should have 0 FAILED orders");
        assertEquals(10, orders.size(), "Should have exactly 10 total orders");

        System.out.println("\n" + "=".repeat(70));
        System.out.println("E2E TEST PASSED: Order processing bug verified!");
        System.out.println("=".repeat(70));
        System.out.println("Bug confirmed: " + pendingCount + " orders remain PENDING (in DLQ)");
        System.out.println("=".repeat(70) + "\n");
    }
}
