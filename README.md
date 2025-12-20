# OrderFlow Backend

> Production-style ordering core built with Spring Boot 3.5 (Java 17), PostgreSQL 17, Flyway-managed schemas, and an optional Kafka outbox pump. This document focuses entirely on the backend so hiring teams can see the systems thinking, consistency patterns, and reliability tooling in one place.

## Overview

OrderFlow models the critical slices of an e-commerce backend: catalog metadata, inventory protection, ACID-compliant order placement, synchronous payment capture, transactional outbox emission, and idempotent downstream processing. Every module is structured behind controller -> service -> repository layers, all aggregates extend a shared `BaseEntity` for UUID IDs plus auditing timestamps, and dev profiles boot seed data plus a concurrency lab to prove the locking strategy. PostgreSQL stores the truth, Flyway versions the schema, and the `kafka` Spring profile enables an outbox publisher that streams payment events to `payment.events`.

---

## Problems solved & concepts applied

- **Double-spend prevention via optimistic locking:** Inventory rows carry a JPA `@Version` column so if two shoppers smash "Buy" on the last Steam Deck, the first commit wins and the second receives `InsufficientStockException` instead of an accidental oversell.
- **Kafka-backed transactional outbox:** The same transaction that writes `Payment` rows stores serialized `OutboxEvent` payloads so the scheduled publisher can drain them into the `payment.events` Kafka topic, guaranteeing downstream services never miss a successful capture even if the broker briefly goes dark.
- **Idempotent projections:** `PaymentEventProcessorImpl` records every processed `payment_id` in `processed_payment_event` before touching the `Order`, so retries or duplicated Kafka messages cannot mark an order paid twice.
- **No phantom/dirty reads:** `OrderServiceImpl.placeOrder` executes inside a `READ_COMMITTED` transaction so the combined order, inventory, and payment work either succeeds atomically or rolls back cleanly.
- **Predictable API failures:** `GlobalExceptionHandler` normalizes validation errors, inventory conflicts, and 404s into `ApiErrorResponse`, making it obvious which rule tripped.
- **Audited data:** `BaseEntity` seeds UUIDs and created/updated timestamps automatically so every aggregate is traceable without extra boilerplate.
- **Automated schema governance:** Flyway migrations (V1-V4) define all tables, relations, and indexes; Testcontainers-backed integration tests prove they run the same way locally and in CI.
- **Flexible catalog filtering with dynamic specifications:** `/products/search` accepts text, category, price, and stock filters, and `ProductSpecifications.build` turns the `ProductSearchCriteria` into a single Spring Data `Specification` that lowercases text queries, joins categories, enforces price bands, and toggles `availableQuantity > 0` when requested so every combination executes as one SQL call without a combinatorial repository explosion.

---

## Architecture at a glance

```
                         +-------------------------------+
                         | REST Controllers              |
                         | (Catalog, Order, Health)      |
                         +---------------+---------------+
                                         |
                         +---------------v---------------+
                         | Application Services          |
                         | CatalogService / OrderService |
                         | PaymentService / Outbox pump  |
                         +-------+-------------+---------+
                                 |             |
                                 |             +--------------------+
                                 |                                  |
                        +--------v------+                  +--------v--------+
                        | Inventory +   |                  | Notification    |
                        | optimistic    |                  | abstraction     |
                        | locking       |                  +--------+--------+
                        +--------+------+                           |
                                 |                                  |
                                 v                                  v
                         +---------------+                 +-----------------
                         | PostgreSQL 17 |<---------------->| Kafka (profile)
                         | (Flyway)     |   Outbox events   | payment.events |
                         +---------------+                 +-----------------
```

`Kafka` is only required when the `kafka` profile is active; otherwise, outbox events accumulate in PostgreSQL for inspection.

---

## Domain modules & responsibilities

| Module | Responsibilities | Key classes |
| --- | --- | --- |
| Catalog | Expose `/products`, pagination, slug-based lookups, DTO projection, and `/products/search` with compound filters | `CatalogController`, `CatalogServiceImpl`, `ProductRepository`, `ProductSpecifications`, `ProductSearchCriteria` |
| Inventory | Maintain one-to-one stock rows per product, enforce optimistic locking, surface business errors | `Inventory` entity, `InventoryRepository`, `InsufficientStockException` |
| Order | Accept checkout payloads, orchestrate totals, manage order status, expose `/orders/:id` | `OrderController`, `OrderServiceImpl`, `Order`, `OrderItem` |
| Payment | Store payment records, emit `PaymentCompletedEvent`, call the notification service, insert outbox rows | `PaymentServiceImpl`, `Payment`, `PaymentEventProcessorImpl` |
| Outbox | Persist serialized events, poll unprocessed rows, publish to Kafka when available | `OutboxEvent`, `OutboxEventRepository`, `OutboxPublisher` |
| Notification | Current implementation logs events but abstracts the dependency for email/SMS/webhooks later | `NotificationService`, `LoggingNotificationService` |
| Common / Platform | Auditing, transactions, scheduling, error envelopes, health checks, Flyway configuration | `BaseEntity`, `JpaConfig`, `GlobalExceptionHandler`, `SchedulingConfig`, `HealthController` |

---

## Persistence design

| Table | Purpose & notable constraints |
| --- | --- |
| `category`, `product` | Product metadata with slug uniqueness and `(category_id, price)` composite index for catalog queries. |
| `inventory` | One row per product with `@Version` column, unique `product_id`, and `available_quantity >= 0` check to guard against bad writes. |
| `orders`, `order_item` | Captures buyer, items, immutable pricing, and totals; indexes on `(user_id, created_at DESC)` to list user history efficiently. |
| `payment` | Separate ledger for payment status, method, and amount; foreign key back to orders plus non-negative amount constraint. |
| `outbox_event` | Stores serialized domain events with `processed_at` timestamp and partial index on unprocessed rows for efficient polling. |
| `processed_payment_event` | Idempotence ledger keyed by `payment_id`; used to short-circuit duplicate `PaymentCompletedEvent` handling. |

All schema changes are defined in `db/migration/V1__...sql` through `V4__...sql` and validated at boot (`spring.jpa.hibernate.ddl-auto=validate`).

---

## Detailed flows

### 1. Checkout -> Payment -> Notification
1. Client issues `POST /orders` with user ID (optional), items, and payment method.
2. `OrderServiceImpl` opens a `READ_COMMITTED` transaction, loads each `Product`, verifies `Inventory`, decrements stock, aggregates totals, and persists `Order` + `OrderItem` rows.
3. `PaymentServiceImpl` persists a `Payment` record, constructs `PaymentCompletedEvent`, and immediately invokes `PaymentEventProcessorImpl` so orders flip to `PAID` before the response returns.
4. The same transaction serializes the event to JSON, inserts it into `outbox_event`, and records a `ProcessedPaymentEvent` placeholder.
5. `LoggingNotificationService` logs the success, and the controller returns `OrderResponseDTO` with immutable totals and line items.
6. Any validation or stock failure triggers an exception, which rolls back the transaction and surfaces an HTTP 4xx or 409 payload to the caller.

### 2. Outbox publishing to Kafka
1. When the `kafka` profile is enabled, `OutboxPublisher` wakes up every second (via `@Scheduled`).
2. It pulls up to 100 rows where `processed_at IS NULL`, determines the destination topic (currently `payment.events` for payment aggregates), and sends serialized payloads through `KafkaTemplate`.
3. Successful sends update `processed_at` inside the transaction, guaranteeing exactly-once publication even across restarts.
4. Failures leave the row untouched, so the next polling cycle retries without data loss.

### 3. Concurrency laboratory
1. `OrderConcurrencyDemoRunner` (dev profile) seeds catalog data and forces a demo product's inventory to `1`.
2. Two threads attempt the same checkout simultaneously through `OrderServiceImpl`.
3. Only one transaction can decrement inventory; the other hits either the optimistic-lock version mismatch or `InsufficientStockException`.
4. Logs show before/after inventory counts, proving the system resists double-spend and lost updates.

### 4. Failure handling & idempotence
1. If `PaymentCompletedEvent` is re-delivered (e.g., Kafka retry), the processor first checks `processed_payment_event`.
2. Already-processed IDs short-circuit with a log entry; new IDs persist before mutating the order, so duplicate notifications/order updates are impossible.
3. Global exception mapping converts any unexpected issue into structured JSON that downstream services can monitor.

### 5. Catalog search & filtering
1. Clients hit `GET /products/search` with any combination of `q`, `categoryId`, `minPrice`, `maxPrice`, `inStockOnly`, and standard `page / size / sort` query params.
2. `CatalogController` validates pagination inputs, parses `sort=field,DESC|ASC`, builds a `PageRequest`, and wraps the query arguments inside `ProductSearchCriteria`.
3. `CatalogService.searchProducts` turns the criteria into a `Specification<Product>` using `ProductSpecifications.build`, adding predicates for case-insensitive text search, category joins, price ranges, and optional in-stock enforcement.
4. The service returns a `Page<ProductResponseDTO>` so the frontend receives consistent paging metadata regardless of which filters are active.

---

## Operational readiness & tooling

- **Profiles:** `dev` enables data seeders and the concurrency runner; `test` is optimized for Testcontainers; `kafka` turns on the outbox publisher.
- **Configuration:** `application-*.properties` describe Postgres, Kafka, and Flyway wiring; `CorsConfig` exposes the APIs to any local clients.
- **Scheduling & health:** `SchedulingConfig` activates background tasks, while `/health` provides a simple readiness check for container orchestrators.
- **Logging & observability:** Structured log messages exist around critical paths (order placement, outbox publishing, payment processing) for fast incident investigation.

---

## Testing & quality gates

- **Flyway + Postgres smoke test:** `DatabaseMigrationIntegrationTest` starts PostgreSQL 17 via Testcontainers, applies all migrations, and executes SQL through `JdbcTemplate` to ensure the schema is usable.
- **Service unit tests:** Mockito-based tests (e.g., `CatalogServiceImplTest`) verify paging logic, repository usage, and exception behavior without needing Spring context.
- **Manual runners:** `CatalogManualTestRunner` and the concurrency demo provide deterministic logs when verifying new changes locally.
- **CI hooks:** `.github/workflows/checklist-logger.yml` keeps issue tracking tidy so work items stay visible to stakeholders.

---

## Run locally (backend only)

1. **Prerequisites:** Java 17, Maven Wrapper, PostgreSQL reachable at the coordinates in `application-dev.properties` (or update the file), optional Kafka broker if you want to exercise the outbox publisher.
2. **Start the API:**
   ```bash
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
3. **Run with Kafka:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,kafka
   ```
4. **Execute the test suite:**
   ```bash
   cd backend
   ./mvnw test
   ```

---

## API surface (current sprint)

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/health` | Lightweight health probe for orchestrators and smoke tests. |
| `GET` | `/products?page=&size=&sort=&categoryId=` | Paginated catalog with validated pagination parameters and category filter. |
| `GET` | `/products/search?q=&categoryId=&minPrice=&maxPrice=&inStockOnly=&sort=` | Full-text and faceted search that combines filters with pagination + sorting. |
| `GET` | `/products/{id}` | Fetch a single product; returns 404 with structured payload if missing. |
| `POST` | `/orders` | Places an order, enforces inventory availability, triggers payment capture, and returns the full order aggregate. |
| `GET` | `/orders/{id}` | Retrieves an order with immutable line items, totals, and status transitions. |

---

## Opportunities for future work

1. Replace `LoggingNotificationService` with a real SMTP/SMS/webhook adapter to demonstrate pluggable downstream channels.
2. Extend the outbox schema with retry/error columns and expose metrics for monitoring processed counts.
3. Add saga-style compensation examples (refund workflow) to showcase long-running transaction orchestration.
