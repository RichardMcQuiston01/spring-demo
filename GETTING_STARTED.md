# Getting Started

For the tech stack, package layout, and how to set up a local Spring Boot
toolchain, see [TECHNOLOGY.md](TECHNOLOGY.md).

## Running locally

Requires Docker and Docker Compose.

```bash
docker compose up --build
```

This builds the app image, starts Postgres with a healthcheck, waits for it
to be ready, then starts the API on `:8080`. Postgres connection details can
be overridden via `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD`
environment variables (see `.env.example`).

Once running:

- API base path: `http://localhost:8080/api/v1/tasks`
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Running tests

```bash
mvn test
```

The repository test suite runs against an in-memory H2 database (documented
fallback — this build environment has no Docker daemon available for
Testcontainers); the controller test suite uses `MockMvc` against a mocked
service layer.

## API reference

Base path: `/api/v1/tasks`

| Method | Path            | Behavior                                                     |
|--------|-----------------|---------------------------------------------------------------|
| POST   | `/`             | Create a task. `201` + created resource, or `400` with field errors. |
| GET    | `/`             | List tasks; optional `?status=` filter (`TODO`, `IN_PROGRESS`, `DONE`). |
| GET    | `/{id}`         | Get one task. `404` if missing.                               |
| PUT    | `/{id}`         | Full update. `404` if missing, `400` on validation failure.   |
| PATCH  | `/{id}/status`  | Update status only.                                            |
| DELETE | `/{id}`         | Delete. `204` on success, `404` if missing.                   |

### Example requests

Create a task:

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Write portfolio README", "description": "Cover setup and endpoints"}'
```

List tasks, optionally filtered by status:

```bash
curl "http://localhost:8080/api/v1/tasks?status=TODO"
```

Get a single task:

```bash
curl http://localhost:8080/api/v1/tasks/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

Full update:

```bash
curl -X PUT http://localhost:8080/api/v1/tasks/3fa85f64-5717-4562-b3fc-2c963f66afa6 \
  -H "Content-Type: application/json" \
  -d '{"title": "Write portfolio README", "description": "Done", "status": "DONE"}'
```

Update status only:

```bash
curl -X PATCH http://localhost:8080/api/v1/tasks/3fa85f64-5717-4562-b3fc-2c963f66afa6/status \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}'
```

Delete a task:

```bash
curl -X DELETE http://localhost:8080/api/v1/tasks/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

### Error responses

Every error path returns a structured, honest error body — no stack traces
leak to the client:

```json
{
  "timestamp": "2026-09-23T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Task with id 3fa85f64-5717-4562-b3fc-2c963f66afa6 not found",
  "path": "/api/v1/tasks/3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

Validation failures (`400`) additionally include a `fieldErrors` array of
`{field, message}` objects.
