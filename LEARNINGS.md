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

### Learning 2: Spring Boot 4.x Removed @EntityScan and @EnableJpaRepositories
**Mistake:** Tried to use `@EntityScan` and `@EnableJpaRepositories` annotations in Spring Boot 4.x

**Context:** Multi-module Maven project needed to scan entities/repositories from common module

**Why it was wrong:**
- Spring Boot 4.x removed these annotations from `org.springframework.boot.autoconfigure.domain`
- Caused compilation error: "package org.springframework.boot.autoconfigure.domain does not exist"
- Spring Boot 4.x has improved auto-configuration that makes these annotations unnecessary

**Correct approach:**
```java
// Spring Boot 4.x - Just use scanBasePackages
@SpringBootApplication(scanBasePackages = "com.lazybird")
public class OrderProcessingApplication { ... }
```

**Reference:** This is now documented in TRADEOFF.md as an architecture decision

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

Last Updated: 2025-02-25 (STC 4 completed)
