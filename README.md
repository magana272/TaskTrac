# Trak

**Version 0.1.0**

A sprint planning and task tracking tool. Create projects, break work into tasks, plan sprints, and track progress. When a sprint is active, in-progress tasks show a live countdown against their estimate. Sprints track completed vs total task counts. Available as a CLI, Swing desktop GUI, and REST API server.

![Trak GUI](docs/screenshots/main_ui.png)

## Quick Start

```bash
make build  # build all executables
```

```bash
make gui-test  # build + launch GUI with test data
```

Or start the server and connect clients:

```bash
make build-server && java -jar trak-server   # Terminal 1: start REST API
java -jar trak-gui                           # Terminal 2: launch GUI
java -jar trak-cli --remote tasks            # Terminal 3: CLI
```

## Build

Requires Java 23+ and Gradle.

```bash
make build          # build all 3 jars
make build-gui      # build GUI jar only
make build-cli      # build CLI jar only
make build-server   # build server jar only
make test           # run tests
make clean          # clean artifacts
make reset          # clean + remove .store and .cache

make gui            # build + launch GUI (local)
make gui-test       # build + launch GUI (local + test data)
make gui-server     # build + launch GUI (remote)
make installer      # native installer via jpackage
```

## Executables

| Executable | Purpose | Default Mode |
|---|---|---|
| `trak-server` | REST API server | Port 8080 |
| `trak-cli` | Command-line client | Local (direct DB) |
| `trak-gui` | Swing desktop client | Remote (needs server) |

On macOS, the GUI uses native full-screen and transparent title bar. The `make gui*` targets include the required `--add-opens` JVM flags automatically.

```bash
java -jar trak-server [port]              # start server
java -jar trak-cli [--remote] <command>   # CLI
java -jar trak-gui [--local] [--test]     # GUI (use make targets for macOS full-screen support)

# macOS (with full-screen and native title bar support)
java --add-opens java.desktop/com.apple.eawt=ALL-UNNAMED --add-opens java.desktop/com.apple.eawt.event=ALL-UNNAMED -jar trak-gui --local --test
```

## Native Installer

Build a platform-native installer (`.dmg` on macOS, `.msi` on Windows, `.deb` on Linux):

```bash
make installer   # outputs to build/installer/
```

Bundles a JRE, the GUI fat jar, and the application icon. No separate Java install required on the target machine. Requires JDK 16+ with `jpackage` on PATH.

## First-Run Setup

On first launch (or reinstall), a setup wizard prompts for:

1. **Connection Mode** -- Local (embedded server) or Remote (connect to existing server)
2. **Storage Backend** -- DuckDB (default), JSON, Parquet, Redis, or MongoDB
3. **Connection Settings** -- Redis URL or MongoDB URI/DB if applicable

Data is stored at `~/Library/Application Support/trak1.0.0/.store/`. The wizard writes `workspace.json` there and is skipped when `--test` is passed. On reinstall (new build), the old store is wiped and the wizard re-appears.

## Authentication

A `guest` account (password: `Guest1!`) is created automatically.

```bash
# Interactive login/signup
java -jar trak-cli

# Direct commands
java -jar trak-cli signup manuel --first_name Manuel --last_name Magana --email m@example.com --password pass
java -jar trak-cli login manuel --password pass
java -jar trak-cli logout
```

## CLI Usage

```bash
# Workspace
java -jar trak-cli projects            # list my projects
java -jar trak-cli tasks               # list my tasks
java -jar trak-cli sprints             # list my sprints
java -jar trak-cli detail -p|-t|-s <id> # project/task/sprint details
java -jar trak-cli cur                 # current task + elapsed time
java -jar trak-cli start <task_id>     # start working
java -jar trak-cli end                 # stop working
java -jar trak-cli complete <task_id>  # mark COMPLETE
java -jar trak-cli info                # all commands

# Entity CRUD
java -jar trak-cli project add WebApp --summary "Web app"
java -jar trak-cli task add --title "Fix bug" --project <id> --assigned_to manuel --deadline 2026-06-01 --estimate 2h
java -jar trak-cli sprint add Sprint1 --project WebApp
java -jar trak-cli sprint update Sprint1 --project WebApp --start_date 2026-06-01 --end_date 2026-06-14 --add_task <task_id>
```

## Storage

Data persisted in `.store/`. Five backends (configurable via `.store/workspace.json`):

| Format | Config | Storage |
|---|---|---|
| **DuckDB** (default) | `"duckdb"` | `trak.duckdb` |
| **JSON** | `"json"` | `user_{name}.json`, `task_{id}.json`, etc. |
| **Parquet** | `"parquet"` | `User.parquet`, `Task.parquet`, etc. |
| **Redis** | `"redis"` | Keys: `trak:users:*`, `trak:tasks:*`, etc. |
| **MongoDB** | `"mongo"` | Collections: `users`, `tasks`, `projects`, `sprints`, `backlogs` |

```json
{ "store_format": "json" }
```

Redis requires `REDIS_URL` env var. MongoDB requires `MONGO_URI` and `MONGO_DB`.

## Environment Variables

Secrets and service credentials are loaded by `EnvLoader` in this order (first match wins):

1. **System environment variables** (always checked first)
2. **`src/main/resources/env.properties`** (bundled in jar for installed app)
3. **`~/Library/Application Support/trak1.0.0/.env`** (app support directory)
4. **`.env`** in working directory (development)

### `.env` / `env.properties` format

```properties
# Google OAuth
clientID=<your-google-client-id>
clientsecret=<your-google-client-secret>

# SMTP (password recovery emails)
TRAK_SMTP_HOST=smtp.sendgrid.net
TRAK_SMTP_PORT=587
TRAK_SMTP_USER=apikey
TRAK_SMTP_PASSWORD=<your-smtp-password>
TRAK_SMTP_FROM=noreply@example.com
```

For development, create a `.env` file in the project root (gitignored). For the native installer, copy `.env` to `src/main/resources/env.properties` before building -- it gets bundled into the jar. `env.properties` is also gitignored to prevent committing secrets.

## Tests

```bash
make test  
```

## Documentation

| Document | Contents |
|---|---|
| [docs/DESIGN.md](docs/DESIGN.md) | Architecture, requirements, data model, package structure |
| [docs/DIAGRAM.md](docs/DIAGRAM.md) | Mermaid diagrams (architecture, MVC, DTO flow, storage, etc.) |
| [docs/usage.md](docs/usage.md) | GUI features, CLI reference, REST API endpoints |
| [docs/store_analysis/ANALYSIS.md](docs/store_analysis/ANALYSIS.md) | Storage backend benchmark (JSON, Parquet, DuckDB, Redis, MongoDB) |
