# PROJECT_STRUCTURE

```text
food-delivery-backend/
├── backend/                         # Spring Boot 3 (Java) application
│   ├── src/main/java/com/example/fooddelivery/
│   │   ├── common/                  # shared utilities (errors, ids, time)
│   │   ├── config/                  # Kafka, Redis, OpenAPI, Jackson config
│   │   ├── domain/                  # domain models + JPA entities
│   │   ├── dto/                     # request/response DTOs
│   │   ├── repository/              # Spring Data repositories
│   │   ├── service/                 # business services (orders, menu, tracking)
│   │   ├── web/                     # REST controllers
│   │   └── FoodDeliveryApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml          # local configuration
│   │   └── db/migration/            # Flyway migrations
│   ├── src/test/java/...            # unit tests
│   ├── pom.xml                      # Maven build (JaCoCo, Spring Boot)
│   └── Dockerfile                   # container image for backend
├── simulator/                       # Data simulator (Python) to generate GPS load
│   ├── driver_simulator.py
│   └── Dockerfile
├── docker-compose.yml               # Runs full system locally
├── API-SPECIFICATION.yml            # OpenAPI specification
├── ARCHITECTURE.md                  # architecture & justification (primary doc)
├── CHAT_HISTORY.md                  # design journey summary
├── README.md
└── PROJECT_STRUCTURE.md
```

## Key modules
- **Order tracking read model** is stored in Redis and returned by `GET /api/orders/{id}/tracking`.
- **GPS ingestion** publishes to Kafka topic `driver.location`.
- **Kafka consumer** updates Redis keys for fast reads.
