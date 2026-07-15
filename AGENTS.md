# AGENTS.md

## Purpose

This file provides repository-specific guidance for coding agents and contributors working on Workout Relay.

Workout Relay is a self-hosted web application that synchronizes planned workouts between TrainerRoad, TrainingPeaks, and Intervals.icu. The production distribution is a Docker image containing an Angular frontend and a Kotlin/Spring Boot backend.

---

## Repository structure

```text
.
├── boot/                     Kotlin/Spring Boot backend
│   ├── src/main/kotlin/      Application source
│   ├── src/main/resources/   Configuration and Liquibase changelogs
│   ├── src/test/kotlin/      Backend tests
│   ├── build.gradle.kts
│   ├── gradlew
│   └── version              Application version
├── ui/                       Angular frontend
│   ├── src/app/              Pages and application components
│   ├── src/infrastructure/   API clients, models, and shared services
│   ├── package.json
│   └── package-lock.json
├── docs/                     Documentation images and supporting files
├── .github/workflows/        CI and release workflows
├── Dockerfile                Multi-stage production build
├── docker-compose.yml        Example self-hosted deployment
├── README.md
├── CHANGELOG.md
└── LICENSE
```

---

## Technology stack

### Backend

- Kotlin 1.9
- Java 21
- Spring Boot 3.2
- Spring Data JPA
- OpenFeign
- SQLite
- Liquibase
- Gradle Wrapper

### Frontend

- Angular 17
- Angular Material
- RxJS
- SCSS
- Karma and Jasmine

### Runtime

- Docker
- A single Spring Boot runtime container
- Angular files served from Spring Boot static resources
- Persistent SQLite database and logs under `/data`

---

## Core functional concepts

### Supported platforms

- TrainerRoad
- TrainingPeaks
- Intervals.icu

### Supported calendar synchronization directions

- TrainerRoad → TrainingPeaks
- TrainerRoad → Intervals.icu
- TrainingPeaks → Intervals.icu
- Intervals.icu → TrainingPeaks

TrainerRoad is currently a source platform only.

### TrainerRoad → TrainingPeaks reconciliation

This direction has special replacement behavior for quick one-day actions and scheduled rolling periods.

Safety rules must be preserved:

1. Create the new workout first.
2. Delete the previous workout only after the new workout is created successfully.
3. Delete only TrainingPeaks workouts positively identified as application-managed.
4. Never delete manual TrainingPeaks workouts.
5. Do not delete existing destination workouts when TrainerRoad returns no source workouts.
6. Multi-day reconciliation must process each day independently.

Do not weaken these protections when refactoring synchronization logic.

### Scheduler

Scheduled sync definitions are persisted as JSON in the `schedule_requests` table.

Each scheduled sync contains:

- source and target platform;
- workout types;
- duplicate-skipping behavior;
- start and end day offsets.

Offsets are relative to the execution date. Existing schedules without offsets must remain compatible and default to `0 → 0`.

The scheduler interval is global for the application instance and is read when the application starts.

### Synchronization history

Calendar-to-calendar executions are persisted in `sync_executions`.

Supported statuses include:

- `RUNNING`
- `SUCCESS`
- `NO_CHANGES`
- `PARTIAL_SUCCESS`
- `FAILED`
- `INTERRUPTED`

At startup, incomplete `RUNNING` executions may be recovered as `INTERRUPTED`.

History retention is configurable and defaults to the most recent 100 executions.

---

## Configuration and environment variables

Important runtime variables include:

```dotenv
JAVA_TOOL_OPTIONS=-Duser.timezone=Europe/Lisbon
SCHEDULER_INTERVAL_HOURS=1
SYNC_HISTORY_RETENTION_LIMIT=100
```

The Docker Compose service must explicitly pass variables into the container. Values defined only in a Komodo or Compose `.env` file are interpolation inputs and do not automatically become container environment variables.

After changing startup configuration, recreate the container. A simple restart does not apply changed container environment variables.

Do not hardcode a user-specific timezone in application code. Use an IANA timezone through `JAVA_TOOL_OPTIONS` or a future explicit application setting.

---

## Persistent data

The runtime data directory is:

```text
/data
```

Important files include:

```text
/data/workout-relay.sqlite
/data/workout-relay.log
```

The database can contain platform authentication cookies and API keys. Treat it and all backups as sensitive.

Never commit:

- SQLite database files;
- log files;
- platform cookies;
- API keys;
- HAR files;
- local `.env` files containing secrets;
- exported production configuration.

---

## Backend architecture

Keep the existing separation of concerns:

```text
domain
    Core models and repository contracts

app
    Application services and use cases

infrastructure
    Platform clients, persistence, configuration, and adapters

rest
    HTTP controllers, request/response DTOs, and error handling
```

### Backend guidelines

- Prefer constructor injection.
- Avoid `!!`. Resolve nullable values explicitly and return meaningful errors.
- Do not use exceptions as normal control flow.
- Use `require(...)` for request invariants and clear validation messages.
- Map invalid client requests to appropriate 4xx responses.
- Keep platform-specific logic inside the relevant adapter or repository.
- Keep orchestration and cross-platform rules in application services.
- Do not expose persistence entities directly through REST endpoints.
- Use DTOs for API responses.
- Preserve enum values stored with `EnumType.STRING`.
- Keep external identifiers explicit. Do not overload `equals()` with partial-ID matching.
- Use named helper methods such as `matchesAnyId(...)` when domain matching differs from structural equality.
- Avoid logging full platform responses, cookies, authorization headers, or configuration objects.
- Keep OpenFeign logging at a safe level by default.

### Transactions

Use `@Transactional` for multi-step database maintenance or updates that must be atomic.

Be aware that Spring proxy-based transactions are not applied to self-invocation within the same bean. Call transactional methods through another Spring bean when required.

---

## Database and Liquibase

All schema changes must use Liquibase.

Changelogs are under:

```text
boot/src/main/resources/db/changelog/schemas/
```

Rules:

1. Never modify a changeset that has already been released and applied.
2. Add a new sequential changelog for schema corrections or additions.
3. Keep column names aligned with JPA mappings.
4. Use explicit nullability.
5. Verify SQLite-compatible SQL and data types.
6. Backward compatibility matters because users keep persistent databases across upgrades.
7. Test application startup against both a new database and an upgraded database.

SQLite has flexible storage types. Date/time fields must be written and read consistently. When a converter exists for a timestamp field, preserve its backward-compatible parsing behavior.

---

## Scheduler robustness

A malformed `request_json` must not block all schedules.

When reading persisted schedules:

- deserialize each row independently;
- log the schedule ID and a safe error message;
- do not log the full stored JSON;
- skip invalid schedules in list and automatic execution flows;
- allow invalid rows to be deleted by ID;
- return a clear error when `Run now` targets an invalid schedule.

Avoid concurrent execution of the same schedule. `Run now` and the automatic job can otherwise overlap.

For a single application instance, an in-memory lock keyed by `scheduleId` is sufficient. A multi-instance deployment requires a database-backed lock.

---

## Frontend architecture

The Angular application uses standalone components.

Relevant areas include:

```text
ui/src/app/
    Pages, layout, and reusable UI components

ui/src/infrastructure/
    HTTP clients, API contracts, configuration models, and shared services
```

### Frontend guidelines

- Use explicit TypeScript types for API requests and responses.
- Avoid `any` and untyped method parameters.
- Keep backend and frontend contracts aligned.
- Use `Observable<T>` with a concrete type.
- Use `finalize()` to reset loading state on both success and error.
- Use `takeUntilDestroyed()` for component subscriptions.
- Avoid nested subscriptions when `switchMap`, `concatMap`, or `forkJoin` provides a clearer flow.
- Avoid triggering recursive form events; use `{ emitEvent: false }` when normalizing values.
- Treat form values as nullable unless the type system proves otherwise.
- Protect submit methods internally even when the button is disabled.
- Show actionable error notifications.
- Use Angular Material components consistently.
- Preserve responsive behavior on mobile.
- Keep user-facing text in English unless a requirement says otherwise.

### Sensitive fields

Authentication cookies and API keys must:

- use password-style inputs by default;
- never be written to the browser console;
- never be included in notifications;
- never be copied into client-side logs.

---

## Styling

Global design tokens and shared layout classes belong in:

```text
ui/src/styles.scss
```

Prefer shared CSS variables for:

- colors;
- surfaces;
- borders;
- text;
- success, warning, and error states;
- platform identity colors;
- light and dark themes.

Before adding page-specific styles, check whether the rule belongs in a shared card, panel, or page-layout class.

Do not add new platform icons ad hoc. Use the same icon or badge convention for TrainerRoad, TrainingPeaks, and Intervals.icu throughout the application.

Known issue: the mobile iOS top bar has previously shown unstable behavior with `position: sticky` inside the sidenav layout. Treat changes to top-bar positioning and scroll containers as high-risk and test them on iOS Safari.

---

## Build and test commands

Run commands from the repository root unless otherwise indicated.

### Backend tests

```bash
cd boot
chmod +x gradlew
./gradlew --no-daemon test
```

On Windows PowerShell:

```powershell
cd boot
.\gradlew.bat --no-daemon test
```

### Backend build

```bash
cd boot
./gradlew --no-daemon bootJar
```

The expected JAR is:

```text
boot/build/libs/workout-relay.jar
```

### Backend development server

```bash
mkdir -p data
cd boot

SPRING_DATASOURCE_URL="jdbc:sqlite:../data/workout-relay.sqlite" \
LOGGING_FILE_NAME="../data/workout-relay.log" \
SCHEDULER_INTERVAL_HOURS=1 \
SYNC_HISTORY_RETENTION_LIMIT=100 \
./gradlew bootRun
```

### Frontend install and build

```bash
cd ui
npm ci
npm run build
```

### Frontend tests

```bash
cd ui
npm test
```

For CI, use the non-interactive test command defined by the repository, such as:

```bash
npm run test:ci
```

when available.

### Frontend development server

```bash
cd ui
npm ci
npm run dev
```

The Angular development server uses the configured proxy for backend API and Actuator requests.

### Complete Docker image

```bash
docker build -t workout-relay:local .
```

### Docker Compose

```bash
docker compose up -d
docker compose logs -f --tail=200 workout-relay
```

---

## Docker guidelines

The Dockerfile is a multi-stage build:

1. build the Angular UI;
2. copy the UI into Spring Boot resources;
3. build the executable JAR;
4. copy the JAR into the runtime image.

When changing the runtime image:

- preserve Java 21 compatibility;
- preserve port `8080`;
- preserve `/data` for persistent state;
- prefer a non-root runtime user;
- ensure the runtime user can write to `/data`;
- keep `/actuator/health` available for health checks;
- avoid copying build tools into the final image.

---

## CI and release rules

The CI quality gate should run before Docker publication:

1. backend tests;
2. frontend tests;
3. frontend production build;
4. Docker build validation.

The release workflow should not publish Git state before validation.

Preferred order:

1. validate the requested semantic version;
2. update `boot/version` in the workflow workspace;
3. run backend tests;
4. run frontend tests and build;
5. build the Docker image;
6. create the release commit and tag;
7. publish the image;
8. push the commit and tag;
9. create the GitHub Release.

Do not create a remote tag or GitHub Release before the image has built successfully.

Application versions use semantic versioning and are stored in:

```text
boot/version
```

Keep `CHANGELOG.md` updated for every published release.

---

## Dependency changes

### Gradle

- Prefer the Gradle Wrapper.
- Keep Java and Kotlin versions compatible.
- Do not edit generated Gradle caches.
- Run backend tests after dependency changes.

### npm

- Use `npm ci` for reproducible installs.
- Update `package-lock.json` only through npm.
- Do not manually edit the lockfile.
- Run frontend tests and the production build after dependency changes.

---

## Testing expectations

Every behavior change should include or update tests.

Prioritize tests for:

- scheduler offset validation;
- backward compatibility of persisted schedule JSON;
- invalid schedule JSON handling;
- duplicate schedule detection;
- prevention of concurrent schedule execution;
- TrainerRoad → TrainingPeaks reconciliation;
- create-before-delete safety;
- empty-source deletion protection;
- synchronization result aggregation;
- history status classification;
- `RUNNING → INTERRUPTED` startup recovery;
- retention of the latest configured number of executions;
- failure of history maintenance without failure of the synchronization itself;
- configuration form normalization and loading state.

A Docker build is not a substitute for running tests.

---

## Logging and error handling

Logs must be useful without exposing secrets.

Allowed:

```text
Scheduled sync 12 failed
TrainingPeaks request returned HTTP 401
Deleted 15 old history entries
```

Not allowed:

```text
Full Cookie header
API key value
Full configuration request
Raw HAR content
Complete platform response containing credentials
```

Error responses should be safe and user-oriented. Detailed stack traces belong in server logs.

---

## Documentation updates

Update documentation when changing:

- supported synchronization directions;
- scheduler behavior;
- environment variables;
- persistence behavior;
- history statuses;
- Docker deployment;
- versioning or release workflow;
- known limitations.

Relevant files:

```text
README.md
CHANGELOG.md
docker-compose.yml
AGENTS.md
```

Keep examples consistent with the actual defaults in `application.yaml` and configuration-property classes.

---

## Change checklist

Before finishing a change:

- [ ] The implementation follows the existing backend/frontend boundaries.
- [ ] No secrets are logged or committed.
- [ ] Database changes use a new Liquibase changeset.
- [ ] Existing databases remain upgradeable.
- [ ] Backend tests pass.
- [ ] Frontend tests pass.
- [ ] Frontend production build passes.
- [ ] Docker image builds.
- [ ] Mobile layout was considered.
- [ ] Scheduler changes preserve backward compatibility.
- [ ] TrainerRoad → TrainingPeaks deletion safeguards remain intact.
- [ ] API contracts are typed and aligned.
- [ ] Loading and error states are handled.
- [ ] README and CHANGELOG are updated when applicable.

---

## Agent operating principles

When modifying this repository:

1. Inspect the current implementation before editing.
2. Prefer small, focused diffs.
3. Do not invent endpoints, fields, or platform behavior.
4. Preserve backward compatibility for persisted SQLite data.
5. Make failure modes explicit and safe.
6. Avoid unrelated formatting or line-ending changes.
7. Do not rewrite Git history.
8. Do not delete user data automatically unless the behavior is explicitly required and protected.
9. Do not weaken authentication-cookie handling or reconciliation safeguards.
10. Explain any validation step that could not be executed.