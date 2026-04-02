# CodeMortem 
> Post-Incident Developer Debrief API with failure pattern analytics

A Spring Boot REST API for engineering teams to log incidents, write structured post-mortems, and surface recurring failure patterns — before they become outages.

---

## Features

- **Incident Management** — Report, track, and resolve incidents with severity levels
- **Post-Incident Debriefs** — Structured root cause analysis with timeline, action items, and lessons learned
- **Recurring Failure Pattern Detection** — Identifies service+category combinations that keep breaking
- **MTTR Analytics** — Mean Time To Resolve per service, overall, and formatted for humans
- **JWT Authentication** — Stateless auth, all endpoints protected
- **Swagger UI** — Full API docs at `/swagger-ui.html`

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2, Spring Security |
| Auth | JWT (jjwt 0.11.5) |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Docs | SpringDoc OpenAPI (Swagger) |
| Build | Maven |
| Tests | JUnit 5, Mockito, MockMvc + H2 |

---

## Getting Started

### Prerequisites
- Java 17+
- MySQL 8 running locally
- Maven 3.8+

### Setup

```bash
git clone https://github.com/sujalkalauni/codemortem
cd codemortem
```

Create the database:
```sql
CREATE DATABASE codemortem;
```

Update `src/main/resources/application.properties`:
```properties
spring.datasource.username=your_mysql_user
spring.datasource.password=your_mysql_password
```

Run:
```bash
mvn spring-boot:run
```

API runs on `http://localhost:8081`
Swagger UI: `http://localhost:8081/swagger-ui.html`

---

## API Endpoints

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a developer account |
| POST | `/api/auth/login` | Login, get JWT token |

### Incidents
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/incidents` | Report a new incident |
| GET | `/api/incidents` | List all incidents |
| GET | `/api/incidents/{id}` | Get incident by ID |
| PATCH | `/api/incidents/{id}/resolve` | Mark as resolved |
| PATCH | `/api/incidents/{id}/status` | Update status |
| GET | `/api/incidents/service/{name}` | Filter by service |

### Debriefs
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/incidents/{id}/debriefs` | Submit a debrief |
| GET | `/api/incidents/{id}/debriefs` | List debriefs for incident |
| GET | `/api/incidents/{id}/debriefs/{debriefId}` | Get specific debrief |
| PUT | `/api/incidents/{id}/debriefs/{debriefId}` | Update debrief |

### Analytics
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/analytics/summary` | Overall incident summary + MTTR |
| GET | `/api/analytics/patterns?lookbackDays=90` | Recurring failure patterns |
| GET | `/api/analytics/mttr` | MTTR per service |

---

## Example Usage

### 1. Register and get token
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"sujal","email":"sujal@dev.com","password":"secret123"}'
```

### 2. Report an incident
```bash
curl -X POST http://localhost:8081/api/incidents \
  -H "Authorization: Bearer <your_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Payment service DB timeout",
    "severity": "HIGH",
    "serviceName": "payment-service",
    "failureCategory": "DATABASE_TIMEOUT",
    "occurredAt": "2026-03-28T02:30:00"
  }'
```

### 3. Get recurring failure patterns
```bash
curl http://localhost:8081/api/analytics/patterns?lookbackDays=90 \
  -H "Authorization: Bearer <your_token>"
```

Response:
```json
[
  {
    "failureCategory": "DATABASE_TIMEOUT",
    "serviceName": "payment-service",
    "occurrenceCount": 4
  }
]
```

### 4. Get MTTR per service
```bash
curl http://localhost:8081/api/analytics/mttr \
  -H "Authorization: Bearer <your_token>"
```

Response:
```json
[
  {
    "serviceName": "payment-service",
    "averageResolutionSeconds": 7200.0,
    "averageResolutionFormatted": "2h 0m"
  }
]
```

---

## Running Tests

```bash
mvn test
```

Tests use H2 in-memory database — no MySQL required for testing.

---

## Project Structure

```
src/
├── main/java/com/codemortem/
│   ├── analytics/         # AnalyticsService — pattern detection + MTTR
│   ├── config/            # SecurityConfig, GlobalExceptionHandler
│   ├── controller/        # AuthController, IncidentController, DebriefController, AnalyticsController
│   ├── dto/               # All request/response DTOs
│   ├── entity/            # User, Incident, Debrief
│   ├── repository/        # JPA repos with custom analytics queries
│   ├── security/          # JwtUtils, JwtAuthFilter
│   └── service/           # AuthService, IncidentService, DebriefService
└── test/
    ├── analytics/         # AnalyticsServiceTest
    ├── controller/        # AuthControllerTest (MockMvc + H2)
    └── service/           # IncidentServiceTest
```

---

## Author

**Sujal Kalauni** — [github.com/sujalkalauni](https://github.com/sujalkalauni)
