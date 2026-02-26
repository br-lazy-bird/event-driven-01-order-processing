# Trade-offs & Architecture Decisions

## Session: 2025-02-25

---

## Architecture Decisions

### 1. Multi-Module Maven Structure
**Decision:** Use Maven multi-module project with common, backend, and worker modules

**Rationale:**
- **Code Sharing**: Order entity and OrderRepository used by both backend and worker
- **Separation of Concerns**: Backend handles REST API, worker handles message consumption
- **Build Independence**: Each module can be built and tested separately
- **Production Pattern**: Common pattern in enterprise Java applications

**Trade-offs:**
- ✅ Pros: No code duplication, clean architecture
- ❌ Cons: More complex build process (parent POM must be installed first)

---

### 2. Lombok Usage
**Decision:** Keep Lombok in the project

**Discussion:**
- User asked: "Why do we need lombok?"
- Discussion about whether to remove it for teaching simplicity

**Rationale:**
- **Production Standard**: Lombok is extremely widely used in Spring Boot production applications
- **Industry Relevance**: Learners will encounter Lombok in real-world code
- **Reduces Boilerplate**: Eliminates 50+ lines of getters/setters per entity

**Simplification Applied:**
- Removed `@Builder` and `@AllArgsConstructor` for simplicity
- Kept only `@Data` and `@NoArgsConstructor` (JPA requirement)

**Trade-offs:**
- ✅ Pros: Production-realistic, less boilerplate, widely used
- ❌ Cons: "Magic" code generation, requires IDE plugin, learning curve

---

### 3. Spring Boot 4.x Entity Scanning for Multi-Module Projects
**Decision:** Use `@EntityScan` and `@EnableJpaRepositories` with new Spring Boot 4.x package locations

**Context:**
- Multi-module Maven project with entities in `common` module
- Backend and worker modules need to access common entities

**Rationale:**
- **Required for Multi-Module Projects**: Spring Boot 4.x still requires explicit scanning when entities are in external modules
- **Package Location Changed**: `@EntityScan` moved from `org.springframework.boot.autoconfigure.domain` to `org.springframework.boot.persistence.autoconfigure`
- **Repository Scanning**: `@EnableJpaRepositories` still in same location: `org.springframework.data.jpa.repository.config`

**Implementation:**
```java
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.lazybird")
@EnableJpaRepositories(basePackages = "com.lazybird")
@EntityScan("com.lazybird.common.model")
public class OrderProcessingApplication { ... }
```

**Trade-offs:**
- ✅ Pros: Explicit configuration, works with external modules, production pattern
- ❌ Cons: More boilerplate than single-module projects, easy to miss new package location

---

## Spring Boot 4.x Migration Notes

### Key Changes from Spring Boot 3.x to 4.x

**For Single-Module Projects:**
- ✅ `@SpringBootApplication` auto-configuration sufficient
- ✅ No explicit `@EntityScan` or `@EnableJpaRepositories` needed

**For Multi-Module Projects:**
- ⚠️ Still need `@EntityScan` and `@EnableJpaRepositories`
- ⚠️ `@EntityScan` package location changed:
  - **Old (3.x)**: `org.springframework.boot.autoconfigure.domain.EntityScan`
  - **New (4.x)**: `org.springframework.boot.persistence.autoconfigure.EntityScan`
- ✅ `@EnableJpaRepositories` unchanged: `org.springframework.data.jpa.repository.config.EnableJpaRepositories`

### Migration Strategy for Multi-Module Projects
1. Update `@EntityScan` import to new package: `org.springframework.boot.persistence.autoconfigure.EntityScan`
2. Keep `@EnableJpaRepositories` with same import
3. Specify base packages that include all modules: `basePackages = "com.lazybird"`
4. Test thoroughly - missing entity scanning causes "Not a managed type" errors

**Critical Learning:** Don't assume Spring Boot 4.x removes these annotations entirely. The package location changed, but multi-module projects still need explicit configuration.

---

Last Updated: 2026-02-26 (Corrected Spring Boot 4.x EntityScan information based on actual implementation)
