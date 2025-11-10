# 🚀 F1 Betting Service

A Spring Boot application that enables F1 event browsing, betting, and settling outcomes.
The service integrates with the **OpenF1 public API** to fetch real-time session and driver data.

---

## 🛠 Required Software

| Tool | Version | Notes |
|------|--------:|-------|
| **Java** | 17+ | Required (project uses modern Java features) |
| **Gradle** | Wrapper included | Run with `./gradlew bootRun` |
| **Spring Boot** | 3.5.x | Uses Hibernate 6, Jakarta EE 10 |
| **H2 Database** | Included | In-memory DB for local development |

---

## ▶️ How to Run

### 1) Clone and run
```bash
git clone https://github.com/your-org/f1betting.git
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

## ⚙️ Configuration (application.yml)

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

**Query Params (all optional):**
- `sessionType` — e.g., `Race`
- `year` — e.g., `2024`
- `country` — e.g., `Italy`

**Example:**
```
GET /api/events?sessionType=Race&year=2024
```

**Example response:**
```json
[
  {
    "sessionKey": "9140",
    "sessionType": "Race",
    "year": "2024",
    "country": "Spain",
    "drivers": [
      { "driverId": "1", "fullName": "Max Verstappen", "odds": 3 }
    ]
  }
]
```

---

### 2) POST `/bets`
Place a new bet for a user.

**Request Body (`PlaceBetRequest`):**
```json
{
  "userId": 1,
  "eventId": 1001,
  "driverId": 44,
  "odds": 3,
  "amount": 50
}
```

**Validations:**
- `userId`, `eventId`, `driverId` — required
- `odds` ≥ 1
- `amount` ≥ 1
- user must have enough balance
- **cannot place two bets for the same (userId, eventId)** — returns 400 Bad Request

**Response (201 Created):**
```json
{
  "id": 12,
  "user": 1,
  "event": 1001,
  "driverId": 44,
  "amount": 50,
  "odds": 3,
  "status": "PENDING"
}
```

---

### 3) POST `/bets/{eventId}/outcome`
Mark an event as settled and update bets and balances.

**Request Body (`EventOutcomeRequest`):**
```json
{ "winnerId": 44 }
```

**Response:**
```json
{
  "eventId": 1001,
  "winnerDriverId": 44,
  "numberOfWinningBets": 2,
  "numberOfLostBets": 5
}
```

