# Verification

Verified locally on September 13, 2026.

| Check | Result |
| --- | --- |
| Backend API tests | 13 passed, using isolated SQLite databases |
| Python lint | Passed |
| Python format check | Passed; 11 files |
| Python dependency consistency | Passed; no broken requirements |
| PostgreSQL migration SQL generation | Passed; UUID and timezone-aware timestamp columns generated |
| Gradle wrapper | Launched Gradle 9.6.0 successfully |
| Android debug build | Passed; installable debug APK generated |
| Android networking tests | 2 passed |
| Android ViewModel tests | 3 passed |
| Android lint | Passed with no errors; dependency update notices remain |

The API tests cover create/read/list/update/delete, persistence across API calls, title normalization, content clearing, invalid bodies, missing and malformed IDs, and pagination. Android tests cover endpoint paths and HTTP methods, JSON mapping, PATCH omission semantics, empty 204 responses, HTTP errors, draft preservation after failure, save retry, repeated save prevention, and invalid-title rejection.

Two upstream Python deprecation warnings remain in the installed Starlette test client (httpx integration and an AnyIO alias). They do not fail tests. Android lint reports 0 errors and 11 warnings: dependency update notices and a backup-configuration compatibility advisory. Dependency versions are pinned. The debug build also reports that an AndroidX native library was packaged without stripping symbols.

## Environment limits

- Live PostgreSQL checks were attempted with portable PostgreSQL 17.11. Windows Application Control blocked `plpgsql.dll` while initializing the temporary cluster. No PostgreSQL server was started, and no machine security settings were changed. Actual PostgreSQL CRUD and online migration upgrade/downgrade remain unverified locally.
- Docker is not installed, so the Compose stack was not launched here.
- The APK was compiled and tested on the JVM; interactive emulator/device behavior has not been tested.
- A GitHub Actions workflow is included to run the same API tests against PostgreSQL 17, check schema drift, exercise migration downgrade/upgrade, and build/test/lint Android. The workflow has not been run remotely.

See README.md for setup and exact check commands.
