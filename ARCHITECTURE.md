# Architecture Overview

## System Vision (Completion Target)

Event-driven order processing system demonstrating Dead Letter Queue (DLQ) handling issues.

**Components:**
- **Common Module**: Shared Maven module with Order entity and OrderRepository
- **Backend Service**: REST API for order management + RabbitMQ publisher
- **Worker Service**: RabbitMQ consumer + fulfillment simulation
- **Frontend**: React TypeScript UI for order management
- **Database**: PostgreSQL 15 with order tracking
- **Message Broker**: RabbitMQ 3.12 with DLQ configuration

**The Bug:** Orders fail fulfillment → routed to DLQ → no DLQ consumer → stuck in PENDING forever

**Tech Stack:**
- Java 21 + Spring Boot 4.0.3 + Maven (Backend & Worker)
- React + TypeScript (Frontend)
- PostgreSQL 15 (Database)
- RabbitMQ 3.12 (Message Broker)
- Docker Compose (Orchestration)

---

## Current Status

### Completed
- ✅ **STC 0**: Project structure and repository setup
  - Multi-module Maven project (common, backend, worker)
  - Directory structure created
  - Git repository initialized with main and develop branches
  - GitHub repository created

### In Progress
- None

### Not Started
- Database schema and environment configuration
- Common module with shared entities
- Backend REST API
- Worker service with fulfillment logic
- RabbitMQ queue configuration
- Docker Compose setup
- E2E tests
- Frontend UI
- Documentation (README, DETONADO)

---

## Architecture Decisions

### Multi-Module Maven
- Root POM coordinates common, backend, and worker modules
- Common module shared between backend and worker (avoids code duplication)
- Dependency: backend → common, worker → common

### SOLID Principles
- **Single Responsibility**: Separate layers (Controller, Service, Repository)
- **Dependency Inversion**: Depend on interfaces (JpaRepository)
- **Interface Segregation**: Minimal, focused interfaces

### Testing Strategy
- E2E tests using RestTemplate (simple, no external dependencies)
- Tests run in Docker environment
- Tests demonstrate the bug (orders stuck in PENDING)

---

Last Updated: STC 0 completion
