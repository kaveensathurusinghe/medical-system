# Full-Stack DevOps CI/CD Pipeline with Docker & Self-Hosted Runner

Full-stack medical management system built with a React (Vite) frontend, multiple Spring Boot microservices, JWT-based auth (auth-service), and MongoDB, packaged for local Docker and wired with GitHub Actions CI/CD and monitoring.

---

## Table of contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Getting started (Docker)](#getting-started-docker)
- [Local development](#local-development)
- [CI/CD pipeline](#cicd-pipeline)
- [Monitoring & dashboards](#monitoring--dashboards)
- [Configuration & secrets](#configuration--secrets)
 - [Screenshots](#screenshots)
- [Troubleshooting](#troubleshooting)

---

## Overview

This repository contains:

- A React (Vite) SPA in `frontend/` for patients, doctors, and admins.
- Spring Boot (Java 21) microservices in `profiles-service/`, `records-billing-service/`, `appointments-service/`, and `auth-service/`.
 - Auth is provided by `auth-service` which issues JWT tokens used by the APIs.
 - A Docker Compose stack that runs frontend + API services + MongoDB + Prometheus + Grafana locally.
- GitHub Actions workflows for CI (build & test) and CD (deploy to a self‑hosted Docker runner).

DevOps‑oriented features:

- Reproducible local stack via Docker Compose.
- CI/CD to GHCR and a self‑hosted Docker runner.
- Built‑in metrics and dashboards (Prometheus + Grafana).

Key files:

- CI workflow: `.github/workflows/ci.yml`
- Deploy workflow: `.github/workflows/deploy-local-docker.yml`
- Compose stack: `docker-compose.yml`

---

## Architecture

High‑level architecture of the system running via Docker Compose:

![Architecture](docs/diagrams/architecture.png)

- Browser → React (Vite) frontend served by Nginx (in the `frontend` container).
- Frontend Nginx → path-based API routing to `profiles-service`, `records-billing-service`, `appointments-service`, and `auth-service`.
- `auth-service` issues JWT tokens consumed by all API services.
- API services → MongoDB database (`mongodb` container).
- API services expose metrics to Prometheus, visualized in Grafana.

---

## Tech stack

- **Frontend**: React, Vite, Nginx (for production container image).
- **Backend services**: Spring Boot (Java 21), Spring Data MongoDB, Spring Security OAuth2 Resource Server.
- **Identity**: `auth-service` (JWT tokens, role claim).
- **Database**: MongoDB official Docker image.
- **Build tools**: Maven (Java services), npm (frontend).
- **Container / Orchestration**: Docker, Docker Compose.
- **CI/CD**: GitHub Actions, GitHub Container Registry (GHCR).
- **Observability**: Spring Boot Actuator, Micrometer Prometheus registry, Prometheus, Grafana.

---

## Project structure

Top‑level layout (simplified):

- `profiles-service/` – profile, admin, session, doctor/patient category endpoints.
- `records-billing-service/` – medical records and payments endpoints.
- `appointments-service/` – appointments and timeslot endpoints.
- `auth-service/` – registration and authentication helper service.
 - (Keycloak removed) authentication is handled by `auth-service`.
- `gateway/` – optional API edge Nginx config.
- `frontend/` – React (Vite) SPA.
- `docker-compose.yml` – local stack (frontend + API services + identity + DB + monitoring).
- `prometheus/` – Prometheus configuration.
- `grafana/` – Grafana provisioning (datasource + dashboards).
- `docs/diagrams/` – PNG diagrams used in this README.

Java services layout (simplified):

- `profiles-service/src/main/java/com/medicalsystem/...` – profile domain API.
- `records-billing-service/src/main/java/com/medicalsystem/...` – records and billing domain API.
- `appointments-service/src/main/java/com/medicalsystem/appointments/...` – appointment domain API.
- `auth-service/src/main/java/com/medicalsystem/auth/...` – auth API.

Frontend layout (simplified):

- `frontend/src/components/...` – React components for patients, doctors, admin.
- `frontend/src/services/api.js` – API helper for calling proxied `/api/*` routes.

---

## Getting started (Docker)

### Prerequisites

- Docker Desktop (recommended) or Docker Engine + Docker Compose.

Environment selection is driven by `COMPOSE_ENV`:

- `COMPOSE_ENV=dev` → uses `.env.dev` (development settings).
- default (no `COMPOSE_ENV` or `COMPOSE_ENV=prod`) → uses `.env.prod` (production‑like settings).

### Quick start – development stack

Build service and frontend images locally, then start everything:

```bash
COMPOSE_ENV=dev docker compose up -d --build
```

Once up:

- Frontend: http://localhost:3000
- Gateway API edge: http://localhost:8080/api
- Auth API (auth-service): http://localhost:8081
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3001

### Quick start – pull and run images (prod‑style)

Assuming CI has already pushed images to GHCR:

```bash
docker compose pull --ignore-pull-failures
docker compose up -d --no-build --remove-orphans
```

This is what the deploy workflow does on the self‑hosted runner.

### Start only monitoring (when app is already running)

```bash
docker compose up -d prometheus grafana node-exporter
```

---

## Local development

### Java services (Spring Boot)

Prerequisite: Java 21 + Maven.

Run services locally (examples):

```bash
cd profiles-service && ./mvnw spring-boot:run
cd ../records-billing-service && ./mvnw spring-boot:run
mvn -f appointments-service/pom.xml spring-boot:run
mvn -f auth-service/pom.xml spring-boot:run
```

Run tests (examples):

```bash
cd profiles-service && ./mvnw test
cd ../records-billing-service && ./mvnw test
mvn -f appointments-service/pom.xml test
mvn -f auth-service/pom.xml test
```

### Frontend (React + Vite)

Prerequisite: Node.js 20+.

Run the frontend dev server:

```bash
cd frontend
npm ci
npm run dev
# Vite dev server: http://localhost:5173
```

---

## CI/CD pipeline

### Overview

The project uses GitHub Actions to build, test, publish Docker images to GHCR, and then deploy them onto a self-hosted runner with Docker.

High‑level pipeline:

![Pipeline](docs/diagrams/pipeline.png)

### Workflows

1. **CI workflow** – `.github/workflows/ci.yml`
   - Runs on pushes and PRs.
  - Builds and tests Java microservices (with a MongoDB service).
   - Builds the frontend.
  - Builds Docker images for profiles, records-billing, appointments, auth, and frontend.
   - Pushes images to GitHub Container Registry (GHCR) with tags:
     - `:sha-<commit>`
     - `:latest`

2. **Deploy workflow** – `.github/workflows/deploy-local-docker.yml`
   - Triggered by `workflow_run` after CI completes on `main`.
   - Runs on a **self‑hosted** runner labeled `local-docker` (same machine as Docker).
   - Steps:
     - `docker compose pull --ignore-pull-failures`
     - `docker compose up -d --no-build --remove-orphans`
    - Polls public/profile endpoints and gateway discovery until the stack is healthy.

### Self‑hosted runner

To enable deployment from GitHub Actions to your local Docker host:

1. In GitHub repo: **Settings → Actions → Runners**.
2. Add a new runner, install it on your machine.
3. Give it the label `local-docker`.
4. Ensure Docker is installed and the runner has permission to run `docker` / `docker compose`.

---

## Monitoring & dashboards

High‑level deployment + monitoring flow:

![Deployment flow](docs/diagrams/deployment-flow.png)

### Metrics

- Service metrics are exposed at `/actuator/prometheus` inside each API service container.
- Prometheus scrapes:
  - `profiles-service`, `records-billing-service`, `appointments-service`, `auth-service`.
  - `node-exporter` (for host metrics, on supported platforms).

### Grafana

- URL: http://localhost:3001
- Default credentials: `admin` / `admin` (Grafana will ask you to change this on first login).
- Dashboards are pre‑provisioned from `grafana/dashboards/medical-system-overview.json`.
  - CPU usage panels (host).
  - Memory usage.
  - HTTP request latency across API services.

### Useful commands

```bash
# Validate docker-compose configuration
docker compose config

# Tail logs for key services
docker compose logs profiles-service --tail=200
docker compose logs records-billing-service --tail=200
docker compose logs grafana --tail=200
```

---

## Configuration & secrets

### Environment files

The Compose stack uses env files for configuration:

- `.env.dev` – development settings (e.g. image tags, Mongo URI, ports).
- `.env.prod` – production‑like settings used by CI/CD.

`docker-compose.yml` loads the appropriate file via `env_file` based on `COMPOSE_ENV`.

### GitHub secrets / registry

- `GHCR_OWNER` – owner/namespace for images in GHCR.
- GitHub Actions must have permissions to push to GHCR.
- Other sensitive values (if any) should be stored as GitHub Actions secrets, **not** committed to the repo.

---

## Screenshots

A few DevOps‑oriented views for this project:

### GitHub Actions – CI workflow

![GitHub Actions CI workflow](docs/diagrams/ci-screenshot.png)

### GitHub Actions – CD / deploy workflow

![GitHub Actions CD workflow](docs/diagrams/cd-screenshot.png)

### Docker containers running the stack

![Docker containers](docs/diagrams/container-screenshot.png)

---

## Troubleshooting

- **cadvisor on macOS**: `cadvisor` is removed from the default stack because Docker Desktop on macOS does not expose the required cgroup mount points. Use `node-exporter` + Prometheus instead, or enable cadvisor only on Linux hosts.
- **Prometheus target DOWN**: open the scraped endpoint directly, e.g.:
  ```bash
  curl -sSf http://localhost:3000/api/health
  ```
- **Containers keep restarting**:
  - Check logs for the service: `docker compose logs <service> --tail=200`.
  - Verify ports not already in use on the host.
- **Deploy workflow fails**:
  - Confirm the self‑hosted runner is online and labeled `local-docker`.
  - Make sure the runner machine has Docker installed and can run `docker compose` without sudo.

