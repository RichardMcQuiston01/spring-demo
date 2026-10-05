# Task API

A portfolio demo showing a clean, production-style Spring Boot REST service: a
CRUD API for managing tasks, built with a layered architecture, input
validation, versioned database migrations, and a containerized deployment
that runs unmodified on a Hostinger VPS.

## Why this exists

This project demonstrates the practices I use on real services, distilled
into a small, readable codebase: a clear separation between controllers,
services, and persistence; explicit input validation with structured error
responses instead of leaked stack traces; schema changes tracked in version
control via Flyway rather than left to Hibernate; and a container setup that
works the same way on a laptop and on a production VPS.

## Tech stack

See [TECHNOLOGY.md](TECHNOLOGY.md) for the tech stack, package layout,
requirements, and how to set up a local Spring Boot toolchain.

## Running locally

See [GETTING_STARTED.md](GETTING_STARTED.md) for how to run the project
locally, run the tests, and the full API reference.

## What this demonstrates

- **Layered architecture** — controllers depend on a service interface, not
  an implementation; persistence is isolated behind Spring Data JPA.
- **Input validation** — Bean Validation on request DTOs, with field-level
  error detail returned to the caller.
- **Schema migrations** — Flyway owns the schema (`ddl-auto: validate`);
  Hibernate never generates or alters tables.
- **Containerized deployment** — a multi-stage Dockerfile and a Compose file
  with no host-specific assumptions, so the same `docker compose up -d` works
  locally and on a Hostinger KVM VPS.

## Storefront demo (`frontend/`)

This repo also holds a small, self-contained frontend demo — a static
storefront built with Vite, TypeScript, and Tailwind CSS. It's unrelated to
the Task API above (no shared backend); it lives in `frontend/` as a second,
independent portfolio piece. See [`frontend/README.md`](frontend/README.md)
for details.

## Buy Me a Coffee

If this app, code, or repository has helped you or someone you know, please consider donating. I appreciate any help to offset the costs of development and/or AI Credits.

[**Donate via Stripe**](https://donate.stripe.com/00w5kD3Gj1Xo9v7gVOcs800), or scan:

[![Donate via Stripe](./donate.svg)](https://donate.stripe.com/00w5kD3Gj1Xo9v7gVOcs800)
