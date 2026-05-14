# DESIGN: bug/stale-test-data-in-store

**Identified at**: 789e75b  
**Branch**: `bug/stale-test-data-in-store`

## Bug

After `make clean && make build && java -jar trak-gui --local --test`, logging in as guest shows stale test data:

```
1778723094407 | Project_1 | testowner | 0 | 0 | 0
```

## Root Cause

After a clean `./gradlew cleanTest test`, `.store/` contains `Project.parquet` and `User.parquet`, and `.cache/` is created at the project root. These are test artifacts that should live under `src/test/`.

**Files that write to `.store` without redirecting** (use default `TTApp.storedir = ".store"`):

| File | What it writes |
|------|---------------|
| `CMDTest.java` | `testowner` user + `Project_1` project via `Main.main()` |
| `StepFunctions.java` | Users, projects, tasks, sprints, backlogs via `Main.main()` + `DAOFactory` |
| `GUIMvcSteps.java` | Users, projects, tasks via `ServiceFactory` (after `registerLocalServices()`) |

**Files that write to `.cache` without redirecting**:

| File | What it writes |
|------|---------------|
| `ObserverPatternTest.java` | `task_viewmodel.ser` via `vm.save()` |

**Files that correctly isolate** (for reference):

| File | Test dir | Cleanup |
|------|----------|---------|
| `ProjectStoreJsonTest` | `.store_test` | `@After` deletes + restores |
| `SeedDataTest` | `.store_seed_test` | `@After` deletes + restores |
| `ServiceListTest` | `.store_list_test` | `@After` deletes + restores |
| `SessionTest` | `.store_session_test` | `@After` deletes + restores |

## Fix

All test artifacts go under `src/test/` using two shared constants:

```java
// Standard test directories — all tests use these
src/test/.store    // test store (TTApp.storedir redirected here)
src/test/.cache    // test cache (ObservableViewModel.CACHE_DIR redirected here)
```

### Files to modify

#### 1. `CMDTest.java` — redirect to `src/test/.store`, add `@After` cleanup

```java
private static final String TEST_STORE = "src/test/.store";
private String originalStoreDir;

@Before
public void setUp() {
    originalStoreDir = TTApp.storedir;
    TTApp.storedir = TEST_STORE;
    new File(TEST_STORE).mkdirs();
}

@After
public void tearDown() {
    File dir = new File(TEST_STORE);
    if (dir.exists()) {
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) f.delete();
        dir.delete();
    }
    TTApp.storedir = originalStoreDir;
}
```

#### 2. `StepFunctions.java` — redirect to `src/test/.store`, cleanup in `@After`

Same pattern: save `TTApp.storedir`, set to `src/test/.store`, restore + delete in `@After`.

#### 3. `GUIMvcSteps.java` — redirect to `src/test/.store`, cleanup in `@After`

`GUIMvcSteps` calls `ServiceFactory.registerLocalServices()` which uses `TTApp.storedir`. Needs `@Before`/`@After` with the same pattern.

#### 4. `ObserverPatternTest.java` — redirect cache to `src/test/.cache`

`ObservableViewModel.CACHE_DIR` is `private static final String`. Options:
- a) Make it package-private or add a setter for tests
- b) Use reflection in the test
- c) Set system property

Simplest: change `CACHE_DIR` from `private` to package-private and set it in the test's `@Before`/`@After`.

#### 5. `ObservableViewModel.java` — make `CACHE_DIR` configurable

Change:
```java
private static final String CACHE_DIR = ".cache";
```
To:
```java
static String CACHE_DIR = ".cache";
```

#### 6. `.gitignore` — add `src/test/.store` and `src/test/.cache`

Ensure test artifacts are never committed even if cleanup fails.

## Tests to add

### `StoreIsolationTest.java` in `task/trak/store/`

1. Verify writes to test store don't appear in `.store`
2. Verify `TTApp.storedir` is restored after teardown
3. Verify test store dir is cleaned up

## Impact

- No production code behavior changes
- All test store/cache writes go to `src/test/.store` and `src/test/.cache`
- `@After` cleanup ensures dirs are deleted after each test class
- `.gitignore` guards against stale artifacts on cleanup failure
