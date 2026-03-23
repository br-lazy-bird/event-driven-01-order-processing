# DETONADO: Event-Driven Order Processing - Dead Letter Queue Recovery

This guide walks you through identifying and fixing the Dead Letter Queue handling issue in the Order Processing application.

---

## Learning Objective

**Primary Skill:** Dead Letter Queue (DLQ) handling in event-driven architectures

By completing this exercise, you will:
- Understand how Dead Letter Queues work in RabbitMQ
- Learn to implement DLQ consumers for failed message recovery
- Practice debugging message-driven applications
- Master error handling patterns in async systems

---

## Problem Identification

### Symptoms

When you create batch orders in the system:
- Some orders immediately show as `COMPLETED`
- Other orders remain stuck in `PENDING` status indefinitely
- The `PENDING` count never decreases
- RabbitMQ's Dead Letter Queue accumulates messages

### Measuring the Problem

1. Start the system: `make run`
2. Open the frontend: http://localhost:3000
3. Click "Place 10 Orders" to create a batch
4. Wait 10-15 seconds and observe the statistics
5. Open RabbitMQ Management UI: http://localhost:15673 (user: `lazybird`, password: `lazybird_rabbitmq`)
6. Navigate to "Queues" tab and check `orders.dlq.queue`

**Or use the API directly:**
```bash
curl -X POST http://localhost:8000/api/orders/batch
sleep 10
curl http://localhost:8000/api/orders | jq
```

**Expected Observation:**
- Approximately 50% of orders are `COMPLETED`
- Approximately 50% of orders are stuck in `PENDING`
- The `orders.dlq.queue` shows messages equal to pending orders
- Messages remain in DLQ indefinitely

### Initial Questions

- Why are some orders completing while others get stuck?
- Where do failed fulfillment attempts go?
- What happens to messages in the Dead Letter Queue?

---

## Understanding the Problem: Dead Letter Queues

### What Is a Dead Letter Queue?

A **Dead Letter Queue (DLQ)** is a special queue that stores messages that couldn't be processed successfully. Messages are routed to a DLQ when:
- Processing fails and message is rejected (nack with requeue=false)
- Message TTL (time-to-live) expires
- Queue reaches maximum length

**Key Concepts:**
- **Main Queue**: Where messages are initially published (`orders.process.queue`)
- **DLQ**: Where failed messages are routed (`orders.dlq.queue`)
- **DLX** (Dead Letter Exchange): Routes rejected messages to DLQ
- **Consumer**: Service that processes messages from a queue

### The Current Architecture

```
┌─────────────┐  Publish   ┌──────────────────────┐  Consume   ┌──────────────┐
│   Backend   │ ────────> │ orders.process.queue │ ────────> │    Worker    │
└─────────────┘            └──────────┬───────────┘            │OrderProcessor│
                                      │                        └──────────────┘
                                      │ On Failure                    │
                                      │ (exception + requeue=false)   │
                                      ▼                               ▼
                             ┌──────────────────┐         Success: Mark COMPLETED
                             │ orders.dlq.queue │         Failure: → DLQ
                             └──────────────────┘
                                      │
                                      │
                                ❌ NO CONSUMER!
                              Messages stuck here forever
```

**The Problem:** Failed messages go to the DLQ, but there's no consumer to process them. Orders remain in `PENDING` status forever.

### Why This Happens

The worker's OrderProcessor (worker/src/main/java/com/lazybird/worker/service/OrderProcessor.java) processes orders:

```java
@RabbitListener(queues = OrderMessagingConstants.PROCESS_QUEUE)
public void process(String orderId) {
    try {
        fulfillmentService.fulfillOrder(orderId);
        orderUpdateService.changeOrderState(UUID.fromString(orderId), OrderStatus.COMPLETED);
        logger.info("Order id: " + orderId + " COMPLETED");
    } catch (FulfillmentException e) {
        logger.error("Fulfillment failed for order: " + orderId + ", sending to DLQ");
        throw e;  // Re-throw → Spring AMQP rejects → DLX routes to DLQ
    }
}
```

**The Flow:**
1. FulfillmentService has 50% random failure rate
2. When fulfillment fails, `FulfillmentException` is thrown
3. Spring AMQP rejects the message (requeue=false)
4. RabbitMQ's DLX routes message to `orders.dlq.queue`
5. **Problem**: No consumer reads from DLQ, so order stays `PENDING` forever

**Configuration:** The RabbitMQ container factory is configured with `setDefaultRequeueRejected(false)` to prevent infinite requeuing:

```java
@Bean
public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setDefaultRequeueRejected(false); // Don't requeue - send to DLX/DLQ
    return factory;
}
```

### What We Need

```
┌─────────────┐  Publish   ┌──────────────────────┐  Consume   ┌──────────────┐
│   Backend   │ ────────> │ orders.process.queue │ ────────> │    Worker    │
└─────────────┘            └──────────┬───────────┘            │OrderProcessor│
                                      │                        └──────────────┘
                                      │ On Failure                    │
                                      │ (exception + requeue=false)   │
                                      ▼                               ▼
                             ┌──────────────────┐         Success: Mark COMPLETED
                             │ orders.dlq.queue │         Failure: → DLQ
                             └──────────┬───────┘
                                        │
                                        │ Consume
                                        ▼
                                ┌──────────────────┐
                                │ DLQProcessor     │ ✅ Retry fulfillment
                                │ (NEW!)           │ ✅ Mark COMPLETED on success
                                └──────────────────┘ ✅ Or mark FAILED after retries
```

**The Solution:** Implement a DLQ consumer that:
1. Retrieves messages from `orders.dlq.queue`
2. Retries fulfillment
3. Marks orders as `COMPLETED` if successful, or `FAILED` after max retries

### Further Reading

- [Microservices Patterns: Messaging](https://microservices.io/patterns/communication-style/messaging.html)
- [RabbitMQ Dead Letter Exchanges Documentation](https://www.rabbitmq.com/docs/dlx)

---

## Diagnosis and Root Cause Analysis

### 1. Check RabbitMQ Queue Status

Open RabbitMQ Management UI (http://localhost:15673):
- Login: `lazybird` / `lazybird_rabbitmq`
- Navigate to "Queues" tab

**Observation:**
```
orders.process.queue - Messages consumed normally
orders.dlq.queue     - Messages accumulating (NOT being consumed) ❌
```

Messages enter `orders.dlq.queue` but never leave. This confirms no consumer is processing the DLQ.

### 2. Inspect Worker Logs

```bash
docker logs -f order_processing_worker
```

**What to look for:**
- "Fulfillment failed for order: ..., sending to DLQ"
- No logs indicating DLQ message consumption
- Only `orders.process.queue` consumption logs

### 3. Verify Database State

```bash
make db-shell
# Then run:
SELECT status, COUNT(*) FROM orders GROUP BY status;
```

**Expected result:**
```
  status   | count
-----------+-------
 COMPLETED |    5
 PENDING   |    5
(2 rows)
```

Pending orders have corresponding messages stuck in `orders.dlq.queue`.

**PS: The amount of pending can be slightly different, because the fulfillment service is not deterministic. Each 
execution has a 50% of probability of failure.**


### 4. Review RabbitMQ Configuration

Check `worker/src/main/java/com/lazybird/worker/config/RabbitMQConfig.java`:

```java
// Process queue with DLX configured
@Bean
public Queue processQueue() {
    return QueueBuilder.durable(OrderMessagingConstants.PROCESS_QUEUE)
            .withArgument("x-dead-letter-exchange", OrderMessagingConstants.ORDERS_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", OrderMessagingConstants.DLQ_KEY)
            .build();
}

// DLQ exists but has NO CONSUMER
@Bean
public Queue deadLetterQueue() {
    return QueueBuilder.durable(OrderMessagingConstants.DLQ_QUEUE)
            .build();
}
```

**Observation:** The DLQ is declared and properly bound, but no `@RabbitListener` is configured to consume from it.

### Understanding the Message Flow

When an order is created:
1. Backend publishes order ID to `orders.process.queue`
2. Worker's `OrderProcessor` receives the message
3. FulfillmentService processes order (50% failure rate)
4. **On success**: Order marked `COMPLETED`, message acknowledged
5. **On failure**: Exception thrown → Spring AMQP rejects → DLX routes to `orders.dlq.queue`
6. **Problem**: No consumer reads from DLQ, so order stays `PENDING` forever

---

## Solution Implementation

We'll implement a DLQ consumer to process failed orders.

### Step 1: Create DLQProcessor Service

Create a new file: `worker/src/main/java/com/lazybird/worker/service/DLQProcessor.java`

```java
package com.lazybird.worker.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.lazybird.common.messaging.OrderMessagingConstants;
import com.lazybird.worker.exceptions.FulfillmentException;
import com.lazybird.worker.model.Order.OrderStatus;

@Service
public class DLQProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DLQProcessor.class);
    private static final int MAX_RETRIES = 3;

    private final FulfillmentService fulfillmentService;
    private final OrderUpdateService orderUpdateService;

    public DLQProcessor(FulfillmentService fulfillmentService, OrderUpdateService orderUpdateService) {
        this.fulfillmentService = fulfillmentService;
        this.orderUpdateService = orderUpdateService;
    }

    @RabbitListener(queues = OrderMessagingConstants.DLQ_QUEUE)
    public void processDLQ(String orderId) {
        logger.info("Processing DLQ message for order: {}", orderId);

        boolean success = retryFulfillment(orderId);

        if (success) {
            orderUpdateService.changeOrderState(UUID.fromString(orderId), OrderStatus.COMPLETED);
            logger.info("Order {} completed after DLQ retry", orderId);
        } else {
            orderUpdateService.changeOrderState(UUID.fromString(orderId), OrderStatus.FAILED);
            logger.warn("Order {} marked as FAILED after {} retries", orderId, MAX_RETRIES);
        }
    }

    private boolean retryFulfillment(String orderId) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                logger.info("Retry attempt {}/{} for order: {}", attempt, MAX_RETRIES, orderId);
                fulfillmentService.fulfillOrder(orderId);
                logger.info("Order {} fulfilled successfully on attempt {}", orderId, attempt);
                return true;
            } catch (FulfillmentException e) {
                logger.warn("Fulfillment failed for order {} on attempt {}", orderId, attempt);
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(1000 * attempt); // Simple backoff: 1s, 2s, 3s
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        return false;
    }
}
```

### Understanding the Implementation

**Key Design Decisions:**

1. **@RabbitListener on DLQ**: Consumes messages from `orders.dlq.queue`

2. **Retry Logic**: Attempts fulfillment up to 3 times with simple backoff (1s, 2s, 3s)

3. **Final State Guarantee**: Every message either succeeds (`COMPLETED`) or exhausts retries (`FAILED`) - no orders stuck in `PENDING`

4. **Simple Acknowledgment**: Uses Spring AMQP's default auto-acknowledgment (simpler than manual ack)

---

## Verification and Expected Results

### Step 1: Rebuild and Restart

```bash
# Stop current system
make stop

# Rebuild with new DLQ consumer
make build
```



### Step 2: Test the Fix

1. Open the frontend: http://localhost:3000
2. If there are remaining orders from the previous execution, press the Reset System button.
3. Click "Place 10 Orders" to create a batch
4. Wait 10-15 seconds and observe the statistics. See that after this period, you won't have any PENDING order 
   because either they failed or succeeded. 

### Step 3: Verify DLQ Processing

Check RabbitMQ Management UI (http://localhost:15673):
```
orders.process.queue - Normal processing
orders.dlq.queue     - Messages consumed and removed ✅
```

Check worker logs:
```bash
docker logs order_processing_worker | grep DLQ
```

You should see logs like:
```
Processing DLQ message for order: abc123
Retry attempt 1/3 for order: abc123
Order abc123 completed after DLQ retry
```

## Success Criteria

You've successfully implemented DLQ recovery when:
- ✅ All orders eventually reach either `COMPLETED` or `FAILED` status
- ✅ No orders remain stuck in `PENDING` indefinitely
- ✅ The `orders.dlq.queue` is properly consumed and empties over time
- ✅ Worker logs show DLQ processing and retry attempts
- ✅ Failed orders (after max retries) are marked as `FAILED`

---

## Production Considerations

### Moving Beyond This Implementation

**1. Configurable Retry Parameters**

Instead of hardcoding retry values, use application properties:

```java
@Value("${dlq.max-retries:3}")
private int maxRetries;

@Value("${dlq.backoff-ms:1000}")
private long backoffMs;
```

```properties
# application.properties
dlq.max-retries=5
dlq.backoff-ms=2000
```

**2. Exponential Backoff**

Improve backoff strategy to avoid retry storm:

```java
long delay = backoffMs * (long) Math.pow(2, attempt - 1);
Thread.sleep(Math.min(delay, maxBackoffMs));
```

**3. Monitoring and Alerting**

Track DLQ metrics:
- Number of messages in DLQ
- Retry success/failure rates
- Average processing time
- Alert when DLQ depth exceeds threshold

**4. Idempotency**

Ensure order processing is idempotent:

```java
public void fulfillOrder(String orderId) {
    Order order = orderRepository.findById(UUID.fromString(orderId)).orElseThrow();

    // Skip if already processed
    if (order.getStatus() == OrderStatus.COMPLETED) {
        logger.info("Order {} already completed, skipping", orderId);
        return;
    }

    // Process order...
}
```

**5. Poisoned Messages**

For messages that fail even DLQ processing, implement a secondary DLQ:

```java
@Bean
public Queue deadLetterQueue() {
    return QueueBuilder.durable(OrderMessagingConstants.DLQ_QUEUE)
        .withArgument("x-dead-letter-exchange", OrderMessagingConstants.ORDERS_EXCHANGE)
        .withArgument("x-dead-letter-routing-key", "orders.poisonpill")
        .build();
}
```

---

## Key Takeaways

**What You Learned:**
- **Dead Letter Queues** provide a safety net for failed message processing
- **DLQ Consumers** recover failed messages and prevent data loss
- **Retry Logic** handles transient failures gracefully
- **Final State Guarantees** ensure no messages are lost or stuck indefinitely

**When to Use This Pattern:**
- Processing external API calls that may fail temporarily
- Database operations that might encounter lock contention
- Any async operation with transient failure modes
- Systems requiring guaranteed message processing

**When NOT to Use This Pattern:**
- Messages with invalid data that should never be retried
- Systems where eventual consistency isn't acceptable
- Real-time processing where retry delays are problematic

---

**Congratulations!** You've successfully implemented Dead Letter Queue recovery. This pattern is essential for building resilient event-driven systems that gracefully handle failures.
