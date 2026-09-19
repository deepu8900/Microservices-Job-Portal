# Microservices JobPortal

A job portal built as a set of independent Spring Boot microservices instead of one monolith — because that's how most real hiring platforms are actually built, and I wanted to understand why.

Candidates can search and apply for jobs, recruiters can post openings and manage applicants, and everyone gets notified in real time when something relevant happens — all wired together with a message broker instead of services calling each other directly.

## Why microservices for a job portal?

A job portal has a few pretty distinct jobs happening under one roof: authentication, job listings, applications, and notifications. In a monolith these all live in one codebase and one database, which is fine until you want to scale job search independently from, say, login traffic, or deploy a fix to notifications without touching anything else. Splitting it up also forced me to actually deal with the hard parts of distributed systems — service discovery, inter-service communication, eventual consistency — instead of reading about them.

## Architecture

```
                        ┌──────────────────┐
                        │   Eureka Server   │  (service registry)
                        └────────┬─────────┘
                                 │
                        ┌────────┴─────────┐
                        │   API Gateway     │  (Spring Cloud Gateway)
                        └────────┬─────────┘
              ┌──────────────────┼──────────────────┬─────────────────┐
              │                  │                  │                 │
      ┌───────┴──────┐   ┌───────┴──────┐   ┌───────┴───────┐  ┌──────┴───────┐
      │ auth-service │   │ job-service  │   │ application-  │  │ notification-│
      │              │   │              │   │ service       │  │ service      │
      │  PostgreSQL  │   │  MySQL+Redis │   │  MySQL        │  │  MySQL        │
      └──────────────┘   └──────┬───────┘   └───────┬───────┘  └──────┬───────┘
                                 │                   │                 │
                                 └───────────┬───────┴─────────────────┘
                                             Kafka
                                  (job.posted, application.submitted)
```

The frontend never talks to a service directly — everything goes through the gateway, and the gateway asks Eureka where each service currently lives. That's what lets services scale up, restart, or move without the frontend caring.

## What each service actually does

**auth-service** — registration and login, issues JWTs, and role checks live here (`CANDIDATE`, `RECRUITER`, `ADMIN`). Backed by PostgreSQL. Passwords are hashed with BCrypt, obviously.

**job-service** — recruiters post jobs here, candidates search them. Search results are cached in Redis so hammering the search endpoint doesn't hammer MySQL every time. Every new posting fires a `job.posted` event onto Kafka.

**application-service** — candidates apply to jobs, recruiters shortlist or reject. Publishes `application.submitted` events. Prevents a candidate from double-applying to the same job at the database level, not just in the frontend.

**notification-service** — doesn't expose much of an API, it mostly just listens. It consumes both Kafka topics above and turns them into notification records — "your job is live," "your application went through" — without job-service or application-service knowing it exists.

**api-gateway** — the single entry point on port 8080. Routes `/api/auth/**`, `/api/jobs/**`, `/api/applications/**`, `/api/notifications/**` to the right service via Eureka.

**eureka-server** — the phone book every other service registers itself with.

## Tech stack

| Layer | Tech |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring Cloud |
| Security | Spring Security, JWT (jjwt) |
| Persistence | Spring Data JPA / Hibernate, PostgreSQL, MySQL |
| Messaging | Apache Kafka |
| Service discovery / routing | Netflix Eureka, Spring Cloud Gateway |
| Caching | Redis |
| Migrations | Flyway |
| Testing | JUnit 5, Mockito, Testcontainers |
| Observability | Spring Boot Actuator, Prometheus, Grafana |
| Frontend | Vanilla HTML/CSS/JavaScript, `fetch` + async/await, no framework |

No React, no Vue — the frontend is deliberately plain so the focus stays on the backend architecture. Three role-based dashboards (candidate, recruiter, admin) all hit the same gateway.

## Running it locally

You'll need Docker and Docker Compose installed. Everything — databases, Kafka, Redis, Prometheus, Grafana, and all six Spring Boot services — comes up with one command:

```bash
docker compose up --build
```

First build takes a few minutes since Maven has to pull dependencies for six modules. Once it's up:

| What | Where |
|---|---|
| API Gateway (all REST traffic) | http://localhost:8080 |
| Eureka dashboard | http://localhost:8761 |
| Prometheus | http://localhost:9090 |
| Grafana (login: admin/admin) | http://localhost:3000 |
| Frontend | open `frontend/index.html` directly in a browser |

The frontend talks to `http://localhost:8080/api`, so as long as the gateway is up, `index.html` works straight from the filesystem — no build step needed.

### Running without Docker

Start Postgres, MySQL, Redis, Zookeeper, and Kafka however you like, then boot the services in this order (Eureka first, everything else can go in any order after):

```bash
cd eureka-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd auth-service && mvn spring-boot:run
cd job-service && mvn spring-boot:run
cd application-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

Flyway runs the schema migrations automatically on startup — no manual SQL needed.

### Running the tests

```bash
mvn test
```

`job-service` and `application-service` have Mockito unit tests around the core business logic, and `auth-service` has a Testcontainers-backed integration test that spins up a real Postgres container to test registration and login end to end.

## API quick reference

```
POST   /api/auth/register
POST   /api/auth/login

POST   /api/jobs
GET    /api/jobs/{id}
GET    /api/jobs/recruiter/{recruiterId}
GET    /api/jobs/search?keyword=&location=
DELETE /api/jobs/{id}

POST   /api/applications
GET    /api/applications/candidate/{candidateId}
GET    /api/applications/job/{jobId}
PATCH  /api/applications/{id}/status?status=SHORTLISTED

GET    /api/notifications/{recipientId}
PATCH  /api/notifications/{id}/read
```

## What I'd add next

- Rate limiting at the gateway level
- A proper API Gateway auth filter instead of trusting each service to validate its own JWT
- Elasticsearch for job search instead of a SQL `LIKE` query — works fine at this scale, wouldn't at real scale
- Refresh tokens (right now a JWT is valid for 24 hours flat, no revocation)
- A real email/SMS integration instead of notification-service just writing rows to a table

## Project structure

```
job-portal/
├── eureka-server/
├── api-gateway/
├── auth-service/
├── job-service/
├── application-service/
├── notification-service/
├── frontend/
├── monitoring/
│   ├── prometheus.yml
│   └── init-mysql.sql
├── docker-compose.yml
└── pom.xml
```

Each service is its own Maven module under a shared parent POM, with its own `Dockerfile`, its own database, and its own `db/migration` folder for Flyway.
