# Lazy Bird: Event-Driven Order Processing

## ⚠️ UNDER CONSTRUCTION ⚠️

**This system is currently under development and not yet ready for use.**

---

An educational project for learning event-driven architecture and message processing through hands-on practice.

---

## Quick Start

### Prerequisites
- Docker and Docker Compose installed
- 4GB+ available RAM
- Ports 8000, 8001, 5433, 5672, and 15673 available

### Setup

This project includes a `.env` file with development configuration.

These settings are for **local development only** and contain no sensitive data. In production applications, always use proper secret management and never commit credentials to version control.

```bash
# Start the system
make run
```

The system will:
- Start PostgreSQL database
- Initialize order processing schema
- Launch Spring Boot backend API
- Start RabbitMQ message broker
- Launch async worker service

**Access the application:**
- Backend API: http://localhost:8000
- RabbitMQ Management: http://localhost:15673 (user: lazybird, password: lazybird_rabbitmq)
- Database: localhost:5433

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                  Spring Boot Backend                            │
│                 (http://localhost:8000)                         │
│         REST API + RabbitMQ Publisher                           │
└─────────┬────────────────────────────────┬──────────────────────┘
          │                                │
          │ JDBC                           │ AMQP
          │ INSERT orders                  │ Publish order IDs
          ▼                                ▼
┌─────────────────────┐      ┌─────────────────────────────────────┐
│   PostgreSQL DB     │      │          RabbitMQ                   │
│  (localhost:5433)   │      │     (localhost:5672)                │
│  orders table       │      │  orders.process.queue               │
│                     │      │  orders.dlq.queue                   │
└─────────────────────┘      └──────────┬──────────────────────────┘
                                        │
                                        │ AMQP
                                        │ Consume order IDs
                                        ▼
                             ┌─────────────────────────────────────┐
                             │     Async Worker Service            │
                             │    (http://localhost:8001)          │
                             │  Fulfillment + Order Updates        │
                             └─────────────────────────────────────┘
```

### Technology Stack

**Backend:** Spring Boot 4.0.3 (Java 21), Maven multi-module

**Worker:** Spring Boot 4.0.3 (Java 21), async message consumer

**Database:** PostgreSQL 15

**Message Broker:** RabbitMQ 3.13

**Infrastructure:**
- Docker Compose for easy setup
- Isolated network environment

---

## Meet the Lazy Bird

The Lazy Bird team built an event-driven order processing system. Orders are created via REST API, published to RabbitMQ, and processed asynchronously by a worker service.

When fulfillment succeeds, orders are marked as `COMPLETED`. But something's not quite right with the system...

**The problem:** Some orders get stuck in `PENDING` status forever. They never complete, and they never fail. They just... sit there.

Can you help the Lazy Bird figure out what's going wrong?

---

## The Problem

When you place orders in the system, you'll notice something strange:

**Observable Symptoms:**
- Some orders immediately show as `COMPLETED`
- Other orders remain stuck in `PENDING` status forever
- The pending count never decreases, even after waiting
- No orders ever show as `FAILED`, even though the worker service fails ~50% of the time

**Your Mission:**
1. Investigate why orders are getting stuck in pending status
2. Diagnose what happens when fulfillment fails
3. Fix the system so all orders eventually reach a final state
4. Verify that orders can fail gracefully and show `FAILED` status

**Important:** Do NOT modify the fulfillment service's 50% failure rate. This simulates real-world service instability. The solution should work WITH the failures, not around them.

---

## Success Criteria

You'll know you've successfully fixed the system when:

- **All orders reach a final state**: Every order eventually becomes either `COMPLETED` or `FAILED`
- **No orders stuck in PENDING**: The pending count eventually reaches zero
- **Failed orders are visible**: Orders that fail fulfillment show as `FAILED`
- **System handles failures gracefully**: The 50% fulfillment failure rate doesn't break the system

The solution should work WITH the failures, not around them.

---

## How to Use the System

### API Endpoints

**Backend Service:**
- `POST /api/orders/batch` - Create 10 random orders
- `GET /api/orders` - List all orders with current status
- `POST /api/reset` - Delete all orders and purge RabbitMQ queues
- `GET /health` - Health check

**Example Usage:**
```bash
# Create batch of 10 orders
curl -X POST http://localhost:8000/api/orders/batch

# List all orders
curl http://localhost:8000/api/orders

# Reset system
curl -X POST http://localhost:8000/api/reset
```

**Worker Service:**
- Runs in background, no direct API
- Consumes messages from RabbitMQ
- Updates order statuses in database

### RabbitMQ Management

**Access the management UI:**
```bash
# Open http://localhost:15673
# Username: lazybird
# Password: lazybird_rabbitmq
```

Use the RabbitMQ management interface to inspect queues and message flow. This can help you understand what's happening to messages in the system.

**Key Queues:**
- `orders.process.queue` - Main processing queue
- `orders.dlq.queue` - Dead Letter Queue for failed messages

### Database Access

**Using psql:**
```bash
make db-shell
```

**Connection Details:**
- Host: localhost
- Port: 5433
- Database: order_processing
- Username: lazybird_dev
- Password: lazybird_password

**Useful queries:**
```sql
-- View all orders
SELECT id, product, quantity, status, created_at, updated_at FROM orders ORDER BY created_at DESC;

-- Count by status
SELECT status, COUNT(*) FROM orders GROUP BY status;

-- View only pending/failed orders
SELECT id, product, status, failure_reason FROM orders WHERE status IN ('PENDING', 'FAILED');
```

---

## Running Tests

The project includes automated integration tests that verify the complete order processing flow.

**Run tests (fast - uses cached images):**
```bash
make test
```

**Rebuild and test (after code changes):**
```bash
make test-build
```

Tests automatically manage an isolated test database and verify:
- Order creation via REST API
- Message publishing to RabbitMQ
- Async order processing
- Database state consistency

---

## Documentation

For detailed diagnostic guidance and step-by-step fix instructions, see the [DETONADO Guide](./DETONADO.md).

---

Ready to start? Run `make run` and dive in!
