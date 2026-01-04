# ARCHITECTURE

    ## 1. Architectural pattern

    **Event-driven + CQRS read models (cache-first)** within a single backend service.

    - **Write side (truth):** Postgres stores orders, restaurants, drivers.
    - **Event stream:** Driver GPS updates are sent to a Kafka topic.
    - **Read side (fast):** Redis stores read models used by latency-sensitive endpoints:
      - `GET /api/orders/{id}/tracking` (tracking view)
      - `GET /api/restaurants/{id}/menu` (menu + status)

    This keeps hot read paths deterministic and protects Postgres under peak read traffic.

    ## 2. Component diagram (logical)

    ```mermaid
    flowchart LR
      subgraph Clients
        C[Customer App
(poll 3-4s)]
        D[Driver App
GPS updates]
        R[Restaurant Portal]
      end

      subgraph Backend[Spring Boot 3 Backend]
        API[REST API]
        KPROD[Kafka Producer
(GPS)]
        KCONS[Kafka Consumer
(GPS)]
        SRV[Domain Services
(Order/Menu/Tracking)]
      end

      subgraph Data
        PG[(Postgres
System of Record)]
        REDIS[(Redis
Read Models/Cache)]
        KAFKA[(Kafka Topic
driver.location)]
      end

      D -->|POST /drivers/{id}/location| API
      API --> KPROD --> KAFKA
      KAFKA --> KCONS --> REDIS

      C -->|GET /orders/{id}/tracking| API --> REDIS
      R -->|menu updates| API --> PG
      API -->|invalidate/update cache| REDIS
      API --> PG
    ```

    ## 3. Communication flows

    ### 3.1 Driver GPS ingest
    1. Driver sends GPS update to backend
    2. Backend publishes event to Kafka (`driver.location`)
    3. Consumer updates Redis:
       - `driver:{driverId}:latest`
       - for each active order mapped to driver: `order:{orderId}:tracking_view`

    ### 3.2 Customer tracking read
    - Customer polls every 3–4 seconds.
    - Backend reads `order:{orderId}:tracking_view` from Redis.

    ### 3.3 Menu + status read
    - Cache-aside from Redis.
    - Menu changes invalidate `restaurant:{id}:menu_plus_status`.

    ## 4. Reliability & performance principles

    - **Cache-first read paths** for p99 targets (menu/status and tracking).
    - **Async event pipeline** for high-throughput GPS ingestion (Kafka).
    - **Idempotency** for ingest: client sends `eventId` (UUID) optional; backend dedupes via Redis TTL key.
    - **Degraded mode**: if Kafka consumer lags, tracking returns last-known location with freshness timestamp.

    ## 5. Technology justification

    ### Spring Boot 3
    - Strong ecosystem for REST, data access, configuration, and operational features.
    - Spring Boot’s production-ready capabilities are commonly provided via Actuator endpoints (health/metrics). (See Spring Boot documentation.)

    ### Kafka
    - Suitable for high-throughput event streams and decoupling producers/consumers.
    - Kafka is commonly modeled as a distributed commit log / event log enabling replay and independent consumption.

    ### Redis
    - Used as an in-memory cache/read-model store to keep p99 latency low and stable.

    ### Postgres
    - System of record with strong transactional guarantees for orders and payments.

    ### References
    - Spring Boot production-ready features (Actuator): https://docs.spring.io/spring-boot/reference/actuator/index.html
    - Kafka event log model overview: https://axonops.com/docs/data-platforms/kafka/concepts/
    - RabbitMQ messaging broker overview (contrast for queue semantics): https://www.rabbitmq.com/
