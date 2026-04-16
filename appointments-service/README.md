Appointments Service
====================

This is the standalone microservice for scheduling (appointments & timeslots).

Quick run (local dev):

```bash
mvn -f appointments-service/pom.xml -DskipTests package
docker build -t appointments-service:local -f appointments-service/Dockerfile appointments-service
docker run --rm -p 8083:8083 -e MONGODB_URI="mongodb://host.docker.internal:27017/medicalsystem" appointments-service:local
```

The service exposes the original endpoints used by the monolith under `/api/appointments` and `/api/timeslots`.
