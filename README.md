# Food Delivery Backend (Java) — Reliable, Event-Driven, Cache-First

This repository contains a **backend-only** reference implementation for a single-region food delivery marketplace.

## What this project demonstrates
- **Order + pickup lifecycle** (minimal but realistic)
- **Live driver tracking after pickup** (customer polls every 3–4s)
- **GPS ingestion** via REST → **Kafka** → consumer updates **Redis** read models
- **Menu + restaurant status** served from **Redis cache/read-model** to keep reads fast
- **Postgres** as the system of record (orders, restaurants, drivers)
- **Unit tests** + **coverage report** (JaCoCo)
- A **data simulator** container that generates load for **50 drivers** at **10 events/sec** total

## Architecture (high level)
- **Spring Boot 3** application exposes REST APIs.
- **Kafka** topic `driver.location` receives driver GPS events.
- Consumer updates:
  - `driver:{driverId}:latest` (Redis)
  - `order:{orderId}:tracking_view` (Redis)
- Customer tracking endpoint reads only from Redis.

See **ARCHITECTURE.md** for full details and design justification.

---

## Prerequisites
- Docker + Docker Compose

## Run locally (single command)

```bash
docker compose up --build
```

This starts:
- backend app on `http://localhost:8080`
- Postgres
- Redis
- Kafka (KRaft single-broker for local dev)
- Simulator (optional; enabled by default)

> If you want to run without simulator:
```bash
docker compose up --build backend postgres redis kafka
```

---

## Quick demo steps

1) **Bootstrap demo dataset** (creates restaurants, drivers, and picked-up orders):

```bash
curl -X POST http://localhost:8080/internal/demo/bootstrap       -H 'Content-Type: application/json'       -d '{"drivers": 50, "restaurants": 10, "orders": 50}'
```

2) Fetch a restaurant menu + status (cache-first):

```bash
curl "http://localhost:8080/api/restaurants/1/menu"
```

3) Fetch tracking for an order (returns last known driver location; demo order IDs start at 1):

```bash
curl "http://localhost:8080/api/orders/1/tracking"
```

---

## Run tests + coverage (locally)

If you prefer running tests outside Docker:

```bash
cd backend
./mvnw test
./mvnw jacoco:report
```

Coverage report will be generated at:
- `backend/target/site/jacoco/index.html`

---

## Simulator

The simulator container sends **driver GPS events** to:
- `POST /api/drivers/{driverId}/location`

Default:
- 50 drivers
- 10 events/sec total

Configure via env vars in `docker-compose.yml`:
- `SIM_DRIVERS`
- `SIM_EVENTS_PER_SEC`
- `SIM_TARGET_BASE_URL`

---

## Notes
- This is a **reference implementation** (not production hardened end-to-end).
- The design emphasizes patterns that support reliability at scale: **cache-first reads**, **async event pipelines**, and **idempotent writes**.
