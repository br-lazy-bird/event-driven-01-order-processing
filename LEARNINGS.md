# Claude Learnings - Order Processing System

This document tracks mistakes and issues encountered during development to avoid repeating them.

---

## Session: 2025-02-25

### Learning 1: Always Ask Before Changing Versions or Libraries
**Mistake:** Changed Spring Boot version from 4.0.3 to 3.2.2 without asking the user first

**Context:** Encountered compilation error and assumed version 4.0.3 didn't exist

**Why it was wrong:** 
- Violated CLAUDE.md guideline: "If you need to change anything related to versions and libraries, you should ask first"
- User had intentionally specified Spring Boot 4.0.3
- Should have asked for clarification instead of making assumptions

**Correct approach:** When encountering version-related issues, ask the user before changing versions

---

### Learning 2: Spring Boot 4.x Changed @EntityScan Package Location
**Mistake:** Assumed Spring Boot 4.x removed `@EntityScan` and `@EnableJpaRepositories` annotations

**Context:** Multi-module Maven project needed to scan entities/repositories from common module

**What actually happened:**
- `@EntityScan` was MOVED (not removed) to a new package in Spring Boot 4.x
- Old package (Spring Boot 3.x): `org.springframework.boot.autoconfigure.domain.EntityScan`
- New package (Spring Boot 4.x): `org.springframework.boot.persistence.autoconfigure.EntityScan`
- `@EnableJpaRepositories` still exists in the same location: `org.springframework.data.jpa.repository.config.EnableJpaRepositories`

**Why it caused confusion:**
- Tried importing from the old package → compilation error: "package org.springframework.boot.autoconfigure.domain does not exist"
- Initially thought the annotations were removed entirely
- Incorrectly documented that scanBasePackages alone was sufficient

**Correct approach for Spring Boot 4.x multi-module projects:**
```java
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.lazybird")
@EnableJpaRepositories(basePackages = "com.lazybird")
@EntityScan("com.lazybird.common.model")
public class OrderProcessingApplication { ... }
```

**Key takeaway:** For multi-module Maven projects in Spring Boot 4.x, you STILL need both annotations to scan entities and repositories from external modules. The package location changed, but the functionality remains necessary.

---

### Learning 3: Maven Multi-Module Docker Builds Require Parent POM Installation
**Mistake:** Attempted to build child modules in Docker without installing parent POM first

**Context:** Multi-stage Docker build for backend service

**Error:** 
```
Could not find artifact com.lazybird:order-processing-parent:pom:1.0.0 in central
```

**Why it happened:** Maven multi-module projects require parent POM in local repository before building children

**Correct approach:**
```dockerfile
# 1. Copy and install parent POM first
COPY pom.xml .
RUN mvn clean install -N

# 2. Then build common module
COPY common/pom.xml ./common/
COPY common/src ./common/src
RUN cd common && mvn clean install -DskipTests

# 3. Finally build backend
COPY backend/pom.xml ./backend/
COPY backend/src ./backend/src
RUN cd backend && mvn clean package -DskipTests
```

---

### Learning 4: Docker COPY - Only Include Necessary Files
**Mistake:** Initially copied entire `common/` directory to Docker image

**Context:** Building backend service that depends on common module

**Why it was wrong:**
- Copied unnecessary files (target/, .git/, IDE files, etc.)
- Increases image size and build time
- Violates Docker best practices

**Correct approach:** Only copy what's needed for the build
```dockerfile
# Wrong
COPY common/ ./common/

# Correct
COPY common/pom.xml ./common/
COPY common/src ./common/src
```

**Reference:** Now documented in CLAUDE.md line 87-89

---

---

## Session: 2026-02-26

### Learning 5: @EntityScan Package Location Corrected
**Context:** During STC 10 (Frontend), attempted to test backend endpoints and discovered backend was failing to start

**Discovery:** Learning 2 was INCORRECT. `@EntityScan` was not removed in Spring Boot 4.x, it was moved to a different package.

**Resolution:** Updated Learning 2 with correct information about package location change

**Verification:** All three backend endpoints now return 200 OK:
- GET /api/orders
- POST /api/orders/batch
- POST /api/reset

---

### Learning 6: NEVER Mention Root Cause or Solution in README
**Mistake:** Included mentions of "Dead Letter Queue", "DLQ", "orders.dlq", and "Missing DLQ Consumer" throughout the README

**Context:** While writing README for STC 11 (Documentation - README.md)

**Why it was catastrophically wrong:**
- **VIOLATED FUNDAMENTAL RULE** from CLAUDE.md: "The root cause issue should be not mentioned in any files but the DETONADO.md"
- Completely ruined the educational value - users would immediately know the solution
- Broke the "broken system" concept - the whole point is for learners to discover the issue
- README is user-facing documentation - must NEVER contain hints about root cause

**What was leaked:**
- Architecture diagram showed "⚠️ Missing DLQ Consumer!"
- "Meet the Lazy Bird" section explicitly stated DLQ consumer was missing
- "Your Mission" told users to "implement a proper Dead Letter Queue consumer"
- Success criteria mentioned "DLQ is properly consumed"
- RabbitMQ section listed `orders.dlq` queue explicitly

**Correct approach for README:**
- **Only describe observable symptoms**: "orders stuck in PENDING", "no FAILED orders appear"
- **Never mention the root cause**: No DLQ, no missing consumer, no solution hints
- **Be vague about internals**: "Use RabbitMQ management interface to inspect queues" (don't name them)
- **Focus on behavior**: What the user sees, not why it's broken
- **Mission should be investigative**: "Investigate why", "Diagnose what happens", "Fix the system"

**Example of correct vs wrong:**
```markdown
# WRONG (gives away solution)
"The Lazy Bird was too lazy to implement the DLQ consumer."
"Implement a proper Dead Letter Queue consumer with retry logic"

# CORRECT (observable symptoms only)
"Some orders get stuck in PENDING status forever."
"Fix the system so all orders eventually reach a final state"
```

**Critical reminder:**
- README.md = User-facing, ZERO hints allowed
- DETONADO.md = Solution guide, root cause revealed
- This is NON-NEGOTIABLE for broken systems projects

---

Last Updated: 2026-02-26 (STC 11 completed, README scrubbed of all root cause hints)
