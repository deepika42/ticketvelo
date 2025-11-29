# TicketVelo: High-Performance Event Reservation Engine

> **A distributed ticketing system engineered to handle massive traffic spikes (e.g., ticket drops) without data inconsistency or double-bookings.**


## Project Overview
TicketVelo is a full-stack simulation of a high-demand booking platform. It addresses the core engineering challenge of **Race Conditions** in distributed systems.

Instead of relying solely on database transactions (which become a bottleneck under load), TicketVelo implements a multi-layered concurrency strategy using **Redis Distributed Locks (SETNX)** and **Optimistic Locking** to ensure 100% data integrity during concurrent booking attempts.


## Key Features
1. Distributed Concurrency Control (Redis) 
    * Problem: 10,000 users trying to book "Seat A1" simultaneously crashes the database.
    * Solution: Implemented a custom Distributed Mutex using Redis SETNX (Set if Not Exists) via RedisTemplate.
    * Impact: Reduces database load by 90% by rejecting conflicting requests at the cache layer before they touch the persistence layer.
    * Safety: Implemented ID Sorting on bulk bookings to mathematically prevent Deadlocks.
    
2. Event-Driven Architecture (Kafka)
    * Problem: Sending confirmation emails synchronously adds 2-3 seconds of latency to the checkout flow.
    * Solution: Decoupled the notification process using Apache Kafka.
    * Impact: The API returns "Success" in <50ms, while the heavy lifting (email delivery) happens asynchronously in a background consumer thread.
    
3. Security & Identity (JWT)
    * Problem: Basic ID-based APIs are insecure.
    * Solution: Implemented Stateless Authentication using JWTs.
      
4. High-Performance Data Seeding
    * Problem: Initializing 5,000 seats using individual save() calls took >30 seconds.
    * Solution: Implemented Batch Processing to group inserts.
    * Impact: Reduced startup/seeding time to <800ms for large venues (2,500+ seats).
    
5. Observability (Prometheus & Grafana)
    * Feature: Real-time monitoring of API Throughput (RPS) and Business Logic Errors.
    * Result: Visualized 409 Conflict spikes during load testing to verify the locking mechanism is active.
    
## Tech Stack
+ Backend : Java 21, Spring Boot 3.4
+ Database : PostgreSQL 16
+ Cache/Lock: Redis 7 (Alpine)
+ Messaging : Apache Kafka
+ Frontend : Next.js 14, Tailwind CSS
+ Monitoring : Prometheus & Grafana
+ Infra : Docker Compose

## Getting Started
### Prerequisites
* **Docker Desktop**
* **Java 21 SDK**
* **Node.js 18+**

### 1. Start Infrastructure
Spin up Postgres, Redis, Kafka, Prometheus, and Grafana containers.
```bash
docker compose up -d
```

### 2. Start Backend
The application will automatically seed the database with 2 realistic venues.
```bash
./mvnw spring-boot:run
```

### 3. Start Frontend
```bash
cd frontend
```
```bash
npm install
```
```bash
npm run dev
```

Now, Access the app at http://localhost:3000
