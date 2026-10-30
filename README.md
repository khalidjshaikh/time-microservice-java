# time-microservice

A small Java microservice that returns the current date and time for the root path (`/`).

## Endpoints

- `GET /` — returns the current date and time as JSON.
- `GET /healthz` — liveness probe.
- `GET /readyz` — readiness probe.

### Example response

```json
{
  "date": "2026-10-29",
  "time": "20:15:00",
  "timestamp": "2026-10-29T20:15:00Z",
  "timezone": "UTC",
  "unix": 1761768900,
  "version": "dev"
}
```

## Configuration

| Env var | Default | Description |
|---------|---------|-------------|
| `PORT`  | `8080`  | HTTP listen port |
| `TZ`    | (local) | Timezone, e.g. `UTC` |
| `VERSION` | `dev` | Version string returned in responses |

## Build

```bash
./scripts/build.sh
```

## Test

```bash
./scripts/test.sh
```

## Docker

```bash
./scripts/package.sh
docker run --rm -p 8080:8080 time-microservice:latest
```

## Deploy to Kubernetes

```bash
./scripts/deploy.sh
```

## License

MIT
```
