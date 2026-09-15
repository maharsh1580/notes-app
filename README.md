# Notes

A small Android Kotlin app backed by FastAPI and PostgreSQL. Create, list, open, edit, and delete notes. The scope is Notes CRUD only; authentication, payments, and cloud deployment are intentionally absent.

## Structure

```text
android/
  app/src/main/java/com/example/notes/
    data/              Retrofit API, DTOs, HTTP client, repository
    ui/                Compose screens and ViewModel
    MainActivity.kt
  app/src/test/        Networking and ViewModel tests
  gradle/wrapper/      Reproducible Gradle launcher
backend/
  app/                 Configuration, SQLAlchemy model, schemas, routes
  migrations/          Alembic PostgreSQL schema migration
  tests/               API behavior and validation tests
  pyproject.toml       Pinned direct dependencies
compose.yaml           Local PostgreSQL and API services
```

## Start the backend

Install Docker with Compose, then run from this directory:

```sh
docker compose up --build -d
```

The API service waits for PostgreSQL and applies migrations before starting. Open <http://localhost:8000/docs> for interactive API docs. `/health` is a process health check, not a database readiness check. Local development credentials are `notes` / `notes`, database `notes`. The database uses a persistent Docker volume. The ports are bound to localhost.

### Windows shortcuts

After installing Docker Desktop, double-click `Start-Backend.cmd` in this folder. It starts Docker Desktop if needed, waits for Docker to be ready, then builds and starts the database and API. Wait for the success message before opening the Android app.

Double-click `Stop-Backend.cmd` to stop the services. This preserves the Docker database volume, so your saved notes remain available the next time you start the backend.

To inspect service status or troubleshoot startup from a terminal in this folder:

```sh
docker compose ps
docker compose logs --tail=100 api db
```

### Local Python setup

For a local Python workflow, use Python 3.12+ and a running PostgreSQL server:

```sh
cd backend
python -m venv .venv
# Windows: .venv\Scripts\activate
# macOS/Linux: source .venv/bin/activate
python -m pip install -c constraints.txt -e ".[dev]"
# Copy .env.example to .env and adjust DATABASE_URL if needed.
alembic upgrade head
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

The app never silently creates database tables: migrations own schema changes.

## Run Android

Open `android` in Android Studio. Use the bundled JDK 25 (or another Gradle 9.6 compatible JDK) and install Android SDK Platform 37 and Build Tools 36.0.0. Sync and run the `app` debug configuration on an emulator (Android 8.0/API 26 or newer).

The default API URL is `http://127.0.0.1:8000/`. Start the backend, then run `adb reverse tcp:8000 tcp:8000` so the emulator or USB device can reach the backend on your computer. Run this command again if the device reconnects and the connection stops working. Build with:

```sh
./gradlew :app:assembleDebug -PNOTES_API_URL=http://127.0.0.1:8000/
```

To use the standard Android emulator host address without an ADB tunnel, build with `-PNOTES_API_URL=http://10.0.2.2:8000/` instead.

On Windows use `gradlew.bat` in place of `./gradlew`. For another server, pass `-PNOTES_API_URL=https://your-server/` (the trailing slash is required). Local HTTP is enabled only in debug builds. Release builds require an HTTPS endpoint; configure its URL explicitly before distributing.

The app supports loading, empty and error states, refresh, paginated loading, editing, and confirmed deletion. A failed save preserves the draft, and repeated save taps are disabled while a request is active. Drafts survive screen rotation through the ViewModel, but are not saved across process death. There is no offline storage or synchronization; changes from other clients appear on refresh. Concurrent edits use last-write-wins semantics.

## API contract

| Method | Path | Result |
| --- | --- | --- |
| GET | `/api/v1/notes?limit=100&offset=0` | Newest-created first, stable ID tie-breaker |
| POST | `/api/v1/notes` | Create, 201 with Location header |
| GET | `/api/v1/notes/{id}` | Read one |
| PATCH | `/api/v1/notes/{id}` | Partial update |
| DELETE | `/api/v1/notes/{id}` | Delete, empty 204 |

Create body: `{"title":"Shopping","content":"Milk"}`. Title is trimmed and must contain 1–200 characters; content allows up to 10,000 characters and defaults to an empty string. PATCH accepts title, content, or both; empty objects, explicit nulls and unknown fields are rejected. Missing IDs return 404, invalid bodies/UUIDs return 422. Responses include `id` (UUID), `title`, `content`, `created_at`, and `updated_at` (timezone-aware timestamps). Pagination allows 1–100 notes per page.

## Checks

From `backend` after installing development dependencies:

```sh
python -m pytest -q
python -m ruff check .
python -m ruff format --check .
alembic upgrade head --sql
```

Default API tests use a fresh SQLite database for fast isolated behavior checks. To test the actual PostgreSQL schema, create a **dedicated test database**, migrate it, and run the same tests against it:

```sh
# DATABASE_URL and TEST_DATABASE_URL must both point to the dedicated test database.
# PowerShell: $env:DATABASE_URL="postgresql+psycopg://notes:notes@localhost:5432/notes_test"
# PowerShell: $env:TEST_DATABASE_URL=$env:DATABASE_URL
# Bash: export DATABASE_URL=postgresql+psycopg://notes:notes@localhost:5432/notes_test
# Bash: export TEST_DATABASE_URL="$DATABASE_URL"
alembic upgrade head
alembic check
python -m pytest -q
```

PostgreSQL tests use an outer transaction and savepoints, rolling back each test. The test database should start empty. From `android`:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Dependency/build references: 
[Android Gradle plugin documentation](https://developer.android.com/build/releases/about-agp),
[FastAPI database documentation](https://fastapi.tiangolo.com/tutorial/sql-databases/).
