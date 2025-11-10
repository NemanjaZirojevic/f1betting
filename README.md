# 🚀 F1 Betting Service

A Spring Boot application that enables F1 event browsing, betting, and settling outcomes.  
The service integrates with the **OpenF1 public API** to fetch real-time session and driver data.

---

## 🛠 Required Software

| Tool | Version | Notes |
|------|---------|-------|
| **Java** | 17+ | Required for running locally |
| **Gradle** | Wrapper included | Run with `./gradlew bootRun` |
| **Spring Boot** | 3.5.x | Uses Hibernate 6, Jakarta EE 10 |
| **H2 Database** | Included | In-memory for local development |
| **Docker** | Optional | Run the app without installing Java |
| **Docker Compose** | Optional | v1 or v2 supported |

> **Note:** The application uses port **8080**.  
> Make sure nothing else on your computer is already using this port.

---

## ▶️ Running Locally

### 1) Clone and start the application

```bash
git clone https://github.com/NemanjaZirojevic/f1betting.git
cd f1betting
./gradlew bootRun
```

Or via IDE (IntelliJ): run `F1bettingApplication`.

### 2) H2 Console

```
http://localhost:8080/h2-console
```

Credentials:

```
JDBC URL: jdbc:h2:mem:f1betting
Username: sa
Password: sa
```

---

## 🐳 Running the Application with Docker

This project includes a `Dockerfile`, allowing you to run the application  
**without installing Java or Gradle** on your machine.

You can run the application using **Docker Compose (recommended)** or **plain Docker**.

---

### 🔹 A) Run with Docker Compose (recommended)

Create a `compose.yml` file in the project root (or use the provided one):

```yaml
services:
  f1betting:
    build: .
    ports:
      - "8080:8080"
```

Now start the application:

#### Compose v1:
```bash
docker-compose up --build
```

#### Compose v2:
```bash
docker compose up --build
```

Stop Docker:

```bash
docker-compose down   # or: docker compose down
```

---

### 🔹 B) Run with plain Docker

Build the Docker image:

```bash
docker build -t f1betting:latest .
```

Run the container:

```bash
docker run --rm -p 8080:8080 --name f1betting f1betting:latest
```

---

### 🔹 C) Test the API

```bash
curl http://localhost:8080/api/events
```

Place a bet:

```bash
curl -X POST http://localhost:8080/api/bets   -H "Content-Type: application/json"   -d '{"userId":1,"eventId":1001,"driverId":44,"odds":3,"amount":50}'
```

Settle an outcome:

```bash
curl -X POST http://localhost:8080/api/events/1001/outcome   -H "Content-Type: application/json"   -d '{"winnerId":44}'
```

---

## ⚙️ Configuration (`application.yml`)

```yaml
event:
  api:
    url: https://api.openf1.org/v1/

spring:
  h2:
    console:
      enabled: true
      path: /h2-console
  datasource:
    url: jdbc:h2:mem:f1betting;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password: sa
```

---

## 📡 API Documentation

**Base URL:** `http://localhost:8080/api`

### 1) GET `/events`
Fetches F1 event sessions plus driver markets (with random odds).

### 2) POST `/bets`
Place a new bet for a user.

### 3) POST `/events/{eventId}/outcome`
Settle an event and update bets and balances.
