# Food Delivery Backend Architecture

## Overview
The backend is designed as a **Spring Boot** application with supporting services:
- **Postgres** → System of record for all transactional data (orders, drivers, restaurants).
- **Kafka** → Event backbone for high‑throughput GPS updates and replayability.
- **Redis** → Read‑model cache for hot paths like restaurant menus and order tracking.

We adopted a **hybrid CQRS pattern**: separating the **write side** (Postgres) from the **read side** (Redis).

---

## CQRS Pattern
- **Write Side**
    - All commands (create order, assign driver, mark pickup/delivery) are persisted in Postgres.
    - Provides strong consistency and durability.
- **Read Side**
    - Pre‑computed views are stored in Redis for low‑latency reads.
    - Examples:
        - `restaurant:{id}:menu_plus_status` → cached menu with availability.
        - `order:{id}:tracking_view` → cached tracking view updated from Kafka events.
- **Trade‑off**
    - Eventual consistency: reads may lag a few seconds behind writes.
    - Benefit: predictable latency (<200ms) and scalability under heavy load.

---

## Component Communication
1. **Menu Flow**
    - Client → `GET /api/restaurants/{id}/menu`
    - Service checks Redis → returns cached view if present.
    - On cache miss → fetch from Postgres, build DTO, write to Redis with TTL.

2. **Tracking Flow**
    - Driver → `POST /api/drivers/{id}/location`
    - Backend publishes `DriverLocationEvent` to Kafka.
    - Consumer reads Kafka → dedupes → updates Redis keys:
        - `driver:{driverId}:latest`
        - `order:{orderId}:tracking_view`
    - Customer → `GET /api/orders/{id}/tracking` → served directly from Redis.

---

## Key Decisions and Trade‑offs
- **Polling vs WebSockets**
    - Polling every 3–4 seconds chosen for MVP: simpler, more reliable on mobile networks.
    - WebSockets/SSE optional for push updates later.
- **Kafka vs simpler queue**
    - Kafka chosen for scale, ordering, and replayability.
    - Trade‑off: heavier operational overhead.
- **Redis read models**
    - Fast reads at scale.
    - Trade‑off: eventual consistency and need for TTL/invalidation.

---

## Demo Workflow
1. Start services with `docker-compose up --build`.
2. Bootstrap demo data via `POST /internal/demo/bootstrap`.
3. Simulate driver GPS updates via API or simulator.
4. Inspect Redis keys with `redis-cli`.
5. Poll tracking endpoint to see freshness and ETA fields.
6. Call menu endpoint twice to show cache miss vs hit.

---

## Testing and Coverage
- **Unit tests**: Pure Mockito, Redis and Kafka mocked.
- **Serialization**: `JavaTimeModule` registered for `Instant`.
- **Coverage**: Target 80–90% for service logic, 100% for critical utilities.
- Reports generated via:
  ```bash
  mvn clean test jacoco:report
  open target/site/jacoco/index.html