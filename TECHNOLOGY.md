# Technology

## Tech stack

- Java 21, Spring Boot 3.4, Maven
- Spring Web, Spring Data JPA, Bean Validation
- PostgreSQL + Flyway for schema migrations
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, MockMvc, Testcontainers (PostgreSQL)
- Docker + Docker Compose

## Package layout

```
com.mcq.taskapi
├── TaskApiApplication.java
├── entity/            Task, TaskStatus
├── repository/         TaskRepository
├── dto/                TaskRequest, TaskStatusUpdateRequest, TaskResponse
├── service/            TaskService (interface) + impl.TaskServiceImpl
├── controller/         TaskController
├── exception/          TaskNotFoundException, GlobalExceptionHandler, ErrorResponse
└── config/             OpenApiConfig, JpaAuditingConfig
```

## Requirements

To build and run this project outside of Docker, you'll need:

- **Java 21 (JDK)** — the language/runtime version pinned in `pom.xml`
- **Maven 3.9+** — no wrapper is committed, so install it separately
- **Docker & Docker Compose** — to run the full stack locally (app + Postgres)
  and to deploy; this is the recommended way to run the project (see
  [GETTING_STARTED.md](GETTING_STARTED.md)). The integration tests also use
  Docker (Testcontainers) and are skipped without it
- **PostgreSQL 16** — only needed if you're running the app outside Docker;
  Docker Compose provisions this for you otherwise, and Flyway owns the
  schema either way

## Installing Spring

Spring Boot isn't something you install separately — it's a set of Maven
dependencies declared in `pom.xml` (via the `spring-boot-starter-parent`
parent POM), resolved automatically the first time you build. What you do
need installed locally is a JDK and Maven:

1. **Install a JDK 21 distribution.** Any OpenJDK build works — e.g.
   [Eclipse Temurin](https://adoptium.net/), or via a version manager like
   [SDKMAN!](https://sdkman.io/) (`sdk install java 21-tem`) or Homebrew
   (`brew install openjdk@21`).
2. **Install Maven 3.9+.** Via SDKMAN! (`sdk install maven`), Homebrew
   (`brew install maven`), or your OS package manager.
3. **Verify the toolchain:**
   ```bash
   java -version
   mvn -version
   ```
4. **Build once to fetch Spring Boot and its starters:**
   ```bash
   mvn clean package
   ```
   This downloads Spring Boot, Spring Web, Spring Data JPA, and every other
   starter declared in `pom.xml` from Maven Central — no manual Spring
   installation, CLI, or Spring Initializr step required, since the project
   is already scaffolded.

An IDE with Spring/Maven support (IntelliJ IDEA, VS Code with the Java
Extension Pack, or Spring Tools) is optional but makes working with the
Spring Boot auto-configuration and Bean Validation annotations easier.
