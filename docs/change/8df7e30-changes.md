# Fix stale test data leaking into production .store and .cache

- **Found at**: 789e75b
- **Fixed at**: 8df7e30
- **Branch**: `bug/stale-test-data-in-store`

## Problem

After `make clean && make test && make build && java -jar trak-gui --local`, stale test data (`testowner`, `Project_1`) appears in the projects table. Additionally, launching with a clean `.store` crashed with `NoSuchFileException: .store/User.parquet`.

## Root Cause

1. `CMDTest`, `StepFunctions`, and `GUIMvcSteps` wrote to `.store` (production dir) without redirecting `TTApp.storedir` or cleaning up in `@After`
2. `ObserverPatternTest` wrote to `.cache` (production dir) without redirecting
3. `GUIMain` never created `.store` on startup — unlike `CLIMain` and `TrakServer` — so a truly clean launch crashed on Parquet read

## Fix

- `CMDTest.java` — redirect to `src/test/.store`, add `@Before`/`@After`
- `StepFunctions.java` — redirect to `src/test/.store`, add cleanup to `@After`
- `ObserverPatternTest.java` — redirect to `src/test/.cache`, add `@Before`/`@After`
- `ObservableViewModel.java` — `CACHE_DIR` changed to `public static` for test access
- `GUIMain.java` — create `.store` directory before registering local services
- `.gitignore` — added `src/test/.store/` and `src/test/.cache/`

## Diagram

```
BEFORE:
  make test --> CMDTest writes testowner + Project_1 to .store/
            --> StepFunctions writes users/projects to .store/
            --> ObserverPatternTest writes to .cache/
  make build --> jars built
  java -jar trak-gui --local --> loads .store/ --> stale row visible

  (or if .store deleted: NoSuchFileException on startup)

AFTER:
  make test --> CMDTest writes to src/test/.store/ (cleaned in @After)
            --> StepFunctions writes to src/test/.store/ (cleaned in @After)
            --> ObserverPatternTest writes to src/test/.cache/ (cleaned in @After)
  make build --> jars built
  java -jar trak-gui --local --> creates .store/ if missing --> clean table
```

## Tests

- **New**: `StoreIsolationTest.java` (4 tests) — verifies writes don't leak, dirs are redirected, no stale data between runs
