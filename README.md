# Medical System (Full-Stack)

A full-stack medical management web application built to demonstrate practical backend, frontend, database, and deployment skills.

## Architecture

This project uses a 3-tier architecture:

1. Frontend: React (Vite) UI
2. Backend: Spring Boot REST API
3. Database: MongoDB

```text
Browser
  -> React Frontend (Vite in dev / Nginx in Docker)
  -> Spring Boot API (/api/*)
  -> MongoDB
```

## Tech Stack

- Frontend: React, React Router, Axios, Tailwind CSS, Framer Motion
- Backend: Spring Boot 3, Spring Web, Spring Security, Spring Data MongoDB
- Database: MongoDB
- Build Tools: Maven, npm
- Containerization: Docker, Docker Compose

## Authentication and Authorization

The application uses session-based authentication with Spring Security.

### Authentication method

1. User logs in using email + password at `POST /api/auth/login`.
2. Backend authenticates credentials with Spring Security.
3. Security context is stored in server session and tied to the session cookie.
4. Frontend sends credentials/cookies on API calls (`withCredentials: true`).
5. Current logged-in role is available at `GET /api/auth/me`.
6. Logout is handled by `POST /api/auth/logout`.

### Role-based authorization

- `ROLE_ADMIN`
- `ROLE_DOCTOR`
- `ROLE_PATIENT`

Both backend endpoint rules and frontend route guards are applied.

## API Endpoints (Core)

### Auth

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`
- `POST /api/auth/register/patient`
- `POST /api/auth/register/doctor`

### Admin

- `GET /api/admin/doctors`
- `GET /api/admin/patients`
- `GET /api/admin/appointments`
- `GET /api/admin/payments`
- `GET /api/admin/dashboard-stats`
- `GET /api/admin/recent-appointments`
- `GET /api/admin/recent-payments`
- `DELETE /api/admin/doctors/{id}`
- `DELETE /api/admin/patients/{id}`
- `DELETE /api/admin/appointments/{id}`

### Doctor categories

- `GET /api/doctor-categories` (public)
- `GET /api/admin/doctor-categories`
- `POST /api/admin/doctor-categories`
- `DELETE /api/admin/doctor-categories/{id}`

### Doctors

- `GET /api/doctors`
- `POST /api/doctors/register`
- `GET /api/doctors/{id}`
- `PUT /api/doctors/{id}`
- `GET /api/doctors/{id}/dashboard-stats`
- `GET /api/doctors/{id}/appointments`
- `POST /api/doctors/{id}/change-password`
- `DELETE /api/doctors/{id}`

### Patients

- `GET /api/patients`
- `POST /api/patients/register`
- `GET /api/patients/{id}`
- `PUT /api/patients/{id}`
- `GET /api/patients/{id}/dashboard-stats`

### Appointments

- `POST /api/appointments/book`
- `GET /api/appointments/{id}`
- `GET /api/appointments/patient/{patientId}`
- `PUT /api/appointments/reschedule`
- `DELETE /api/appointments/{id}`

### Medical records

- `POST /api/records`
- `GET /api/records`
- `GET /api/records/{id}`
- `GET /api/records/patient/{patientId}`

### Payments

- `GET /api/payments/patient/{patientId}`

### Timeslots

- `GET /api/timeslots/doctor/{doctorId}/available`
- `GET /api/timeslots/doctor/{doctorId}`

## Design Patterns Used

- Layered architecture (Controller -> Service -> Repository)
- MVC style API design with Spring controllers
- Repository pattern with Spring Data MongoDB repositories
- Service layer pattern for business logic
- Dependency Injection (constructor/field injection managed by Spring)
- DTO pattern for request/response payloads

## OOP Concepts Used

- Encapsulation: domain models and DTOs encapsulate state with getters/setters
- Abstraction: services hide business and persistence details from controllers
- Polymorphism: Spring interfaces (`AuthenticationManager`, `PasswordEncoder`, `MongoRepository`) provide pluggable behavior
- Composition: controllers are composed with services, services with repositories

## Project Structure

```text
medical-system/
  backend/   # Spring Boot API + security + MongoDB integration
  frontend/  # React application
  docker-compose.yml
```

## Run with Docker (Recommended)

### Prerequisites

- Docker Desktop (or Docker Engine + Compose)

### Start

```bash
docker compose up -d --build
```

### Access

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- MongoDB: mongodb://localhost:27017

### Stop

```bash
docker compose down
```

### Stop and remove DB volume

```bash
docker compose down -v
```

Note: First Docker build is the slowest because base images and dependencies are downloaded.

## Run Locally (Without Docker)

### Prerequisites

- Java 21
- Node.js 20+
- npm
- MongoDB running locally

### 1) Start MongoDB

If MongoDB is not installed locally, you can run it with Docker:

```bash
docker run -d --name medical-mongo -p 27017:27017 mongo:7.0
```

### 2) Start backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend runs on: http://localhost:8080

### 3) Start frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: http://localhost:5173

## Default Admin Credentials (Development)

- Email: `admin@medicalsystem.com`
- Password: `Admin@123`

These values come from backend application properties and should be changed for production.

## Notes

- This project is intended for learning and portfolio demonstration.
- For production, use HTTPS, secure secret management, stronger audit logging, and stricter ownership checks on all resource-level APIs.
