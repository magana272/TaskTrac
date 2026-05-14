# DESIGN: DuckDB + Redis Stores, Store Benchmarks, UI Redesign

---

## Goal

Add DuckDB (new default) and Redis as storage backends, benchmark all 5 stores with response time data, and redesign the GUI into a project-scoped dashboard with larger task cards and a sprint progress timeline.

## Success Criteria

- DuckDB store works as default (embedded, zero config)
- Redis store works with running Redis server (env vars for connection)
- Benchmark script tests all stores: Parquet, JSON, MongoDB, DuckDB, Redis
- Benchmark results in `docs/store_analysis/` with response time data
- GUI shows project-scoped view: project selector → task cards → sprint progress
- Task cards are larger, more prominent than current 270x190 cards
- Sprint section shows progress bar with completion percentage
- Solo dev can watch task completion and effort in real time

## Constraints

- Follow existing `EntityDAO<T>` interface (save, loadByKey, deleteByKey, loadAll)
- Follow existing `DAOFactory.Format` enum pattern for store selection
- No changes to service layer — stores are swapped transparently via DAOFactory
- Dependencies approved: DuckDB (`org.duckdb:duckdb_jdbc`), Redis (`redis.clients:jedis`)

## UX Expectations

```
┌──────────────────────────────────────────────────────┐
│ TRAK  │ ● user                    [⚙ Settings] [Logout] │
├──────────────────────────────────────────────────────┤
│ Project: [ MobileApp ▼ ]           [+ Add Project]   │
├──────────────────────────────────────────────────────┤
│                                                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │  Task 1     │  │  Task 2     │  │  Task 3     │  │
│  │  Setup RN   │  │  Login UI   │  │  CI/CD      │  │
│  │             │  │             │  │             │  │
│  │  ● READY    │  │  ● PROGRESS │  │  ✓ DONE     │  │
│  │  4h est     │  │  8h est     │  │  3h actual  │  │
│  │  Due: 5d    │  │  Due: 7d    │  │  Completed  │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  │
│                                                       │
│  [+ Add Task]                     Sort: [Due Date ▼]  │
├──────────────────────────────────────────────────────┤
│ Sprint: Sprint 1  (May 19 – Jun 1)                    │
│ ████████████░░░░░░░░░  3/5 tasks   60%               │
│ Ready: 1  In Progress: 1  Complete: 3                 │
└──────────────────────────────────────────────────────┘
```

Key changes from current UI:
- **Project selector at top** replaces separate Projects tab
- **Tasks and Sprint on same screen** instead of separate tabs
- **Larger task cards** (350x220 min) with more visible status, estimate, deadline
- **Sprint progress bar** below tasks — shows real-time completion
- **No more Projects/Sprints table views** — replaced by project-scoped dashboard

---

## Architecture Decisions

### Feature Branch 1: `feature/duckdb-store`

#### New dependency
- `org.duckdb:duckdb_jdbc:1.2.2` (~20MB, embedded SQL, no external server)
- **Why:** Embedded columnar database, faster than Parquet for CRUD operations, SQL interface
- **Alternatives:** SQLite (row-based, less suited for analytics), H2 (heavier)

#### New files (5 DAO classes + 1 helper)
| File | Purpose |
|------|---------|
| `dao/duckdb/DuckDBConnection.java` | Singleton JDBC connection to `.store/trak.duckdb` |
| `dao/duckdb/DuckDBUserDAO.java` | User CRUD via SQL |
| `dao/duckdb/DuckDBProjectDAO.java` | Project CRUD via SQL |
| `dao/duckdb/DuckDBTaskDAO.java` | Task CRUD via SQL |
| `dao/duckdb/DuckDBSprintDAO.java` | Sprint CRUD via SQL |
| `dao/duckdb/DuckDBBacklogDAO.java` | Backlog CRUD via SQL |

#### Modified files
| File | Change |
|------|--------|
| `dao/DAOFactory.java` | Add `DUCKDB` to Format enum, add switch cases |
| `app/client/config/WorkspaceConfig.java` | Default changes from `"parquet"` to `"duckdb"` |
| `app/server/server/TrakServer.java` | Handle `"duckdb"` format string |
| `build.gradle.kts` | Add DuckDB dependency |

#### Data flow
```
DuckDBConnection.getConnection() → java.sql.Connection to .store/trak.duckdb
DuckDBTaskDAO.save(task) → INSERT OR REPLACE INTO tasks (id, title, ...) VALUES (?, ?, ...)
DuckDBTaskDAO.loadByKey(id) → SELECT * FROM tasks WHERE id = ?
DuckDBTaskDAO.loadAll() → SELECT * FROM tasks
DuckDBTaskDAO.deleteByKey(id) → DELETE FROM tasks WHERE id = ?
```

#### Schema (auto-created on first connection)
```sql
CREATE TABLE IF NOT EXISTS users (user_name VARCHAR PRIMARY KEY, first_name VARCHAR, ...);
CREATE TABLE IF NOT EXISTS projects (id BIGINT PRIMARY KEY, project_name VARCHAR, ...);
CREATE TABLE IF NOT EXISTS tasks (id BIGINT PRIMARY KEY, project_name VARCHAR, ...);
CREATE TABLE IF NOT EXISTS sprints (id BIGINT PRIMARY KEY, name VARCHAR, ...);
CREATE TABLE IF NOT EXISTS backlogs (id BIGINT PRIMARY KEY, name VARCHAR, ...);
```

---

### Feature Branch 2: `feature/redis-store`

#### New dependency
- `redis.clients:jedis:5.2.0` (~1MB, requires running Redis server)
- **Why:** In-memory data store, sub-millisecond reads, excellent for high-frequency operations
- **Alternatives:** Lettuce (async, more complex), Redisson (heavier)

#### New files (5 DAO classes + 1 helper)
| File | Purpose |
|------|---------|
| `dao/redis/RedisConnection.java` | Singleton JedisPool from `REDIS_URL` env var |
| `dao/redis/RedisUserDAO.java` | User CRUD via Redis hashes |
| `dao/redis/RedisProjectDAO.java` | Project CRUD |
| `dao/redis/RedisTaskDAO.java` | Task CRUD |
| `dao/redis/RedisSprintDAO.java` | Sprint CRUD |
| `dao/redis/RedisBacklogDAO.java` | Backlog CRUD |

#### Modified files
| File | Change |
|------|--------|
| `dao/DAOFactory.java` | Add `REDIS` to Format enum, add switch cases |
| `app/server/server/TrakServer.java` | Handle `"redis"` format string |
| `build.gradle.kts` | Add Jedis dependency |

#### Data pattern
- Each entity stored as Redis Hash: `trak:tasks:{id}` → `{field: value, ...}`
- Entity lists via key pattern scan: `KEYS trak:tasks:*`
- Config: `REDIS_URL` env var (default: `redis://localhost:6379`)

---

### Feature Branch 3: `feature/store-benchmark`

#### New files
| File | Purpose |
|------|---------|
| `src/test/java/task/trak/benchmark/StoreBenchmark.java` | JUnit test that benchmarks all stores |
| `docs/store_analysis/README.md` | Benchmark methodology and results |
| `docs/store_analysis/results.csv` | Raw timing data |

#### Benchmark methodology
- Operations tested: create 1000 tasks, loadAll, loadByKey (100x), deleteByKey (100x)
- Stores tested: JSON, Parquet, DuckDB, MongoDB (if available), Redis (if available)
- Metrics: avg response time (ms), p50, p95, p99
- Results written to CSV and markdown summary

---

### Feature Branch 4: `feature/ui-redesign`

#### Modified files
| File | Change |
|------|---------|
| `view/MainFrame.java` | Replace CardLayout tabs with single project-scoped dashboard |
| `view/task/TasksView.java` | Larger cards, embedded in dashboard (not separate tab) |
| `view/task/TaskCardPanel.java` | Increase card size to 350x220, more prominent fields |
| `view/sprint/SprintProgressPanel.java` (NEW) | Sprint progress bar with completion % |
| `view/project/ProjectSelectorPanel.java` (NEW) | Project dropdown selector at top |
| `view/DashboardView.java` (NEW) | Combines project selector + tasks + sprint progress |

#### Component responsibilities
- **ProjectSelectorPanel** — dropdown with project list, fires project change events
- **TasksView** — renders task cards filtered by selected project (reused, larger cards)
- **SprintProgressPanel** — horizontal progress bar, task counts by status, sprint dates
- **DashboardView** — composes the three above in a vertical layout
- **MainFrame** — hosts DashboardView as primary view (replaces tab switching)

#### State management
- Selected project stored in `ProjectViewModel` (new field: `selectedProject`)
- Sprint progress derived from `SprintViewModel` + `TaskViewModel` (filtered by project)
- Real-time updates via existing Observer pattern

---

## Testing Strategy

### Cucumber scenarios (new)
- `duckdb_store.feature` — CRUD operations with DuckDB backend
- `redis_store.feature` — CRUD operations with Redis backend
- `store_switching.feature` — switch between stores, verify data persistence

### Unit tests
- DuckDB: schema creation, CRUD for all 5 entity types, connection pooling
- Redis: connection, CRUD for all 5 entity types, serialization
- Benchmark: timing accuracy, CSV output format
- UI: ProjectSelectorPanel fires events, SprintProgressPanel calculates correctly

---

## Tradeoffs

| Decision | Alternative | Rationale |
|----------|-------------|-----------|
| DuckDB as default over Parquet | Keep Parquet default | DuckDB is faster for CRUD, has SQL interface, still embedded |
| Jedis over Lettuce for Redis | Lettuce (async) | Jedis is simpler, synchronous matches our DAO interface |
| Redis hashes over JSON strings | Store JSON in Redis | Hashes allow field-level reads, more idiomatic |
| Single dashboard vs tabs | Keep tab layout | Project-scoped view is better for solo devs |
| Sprint progress bar vs timeline | Gantt chart | Progress bar is simpler, shows what matters |

## Risks

- DuckDB JDBC driver is ~20MB, increases jar size significantly
- Redis requires external server (unlike DuckDB/JSON/Parquet which are embedded)
- UI redesign is breaking change — removes familiar Projects/Sprints tabs
- DuckDB concurrent write behavior differs from file-based stores

---

## Implementation Order

1. `feature/duckdb-store` — new default, all CRUD, tests
2. `feature/redis-store` — Redis support, tests
3. `feature/store-benchmark` — benchmark all 5 stores, generate docs/store_analysis
4. `feature/ui-redesign` — project-scoped dashboard, larger cards, sprint progress

Each branch merges to dev independently after tests pass and user approval.
