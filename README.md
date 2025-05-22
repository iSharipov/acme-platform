## Requirements
Java 17, Docker
## Build
```bash
./gradlew clean build
```
## Run
```bash
docker compose up --build
```
## API

<a href="http://localhost:8080/swagger-ui/index.html">Swagger</a>

## Metrics && Logs

<a href="http://localhost:3000/">Grafana</a>
#### Prometheus Datasource - http://prometheus:9090

Download and install dashboards #19004, #4701. Use Prometheus Datasource

## Logs

#### Loki Datasource - http://loki:3100
Grafana -> Explore, use **container** as a label and acme-platform as a value

## Acceptance Flow Test

**io.github.isharipov.acme.platform.integration.AcceptanceFlowTestIT**