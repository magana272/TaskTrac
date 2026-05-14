# Trak

**Version 0.0.9**

A lightweight task and sprint tracking system with CLI, GUI, and REST API. Supports Parquet, JSON, and MongoDB persistence.

## Quick Start

```bash
make build                                        # build all executables
make build-gui && java -jar trak-gui --local --test   # launch GUI with test data
```

Or start the server and connect clients:

```bash
make build-server && java -jar trak-server   # Terminal 1: start REST API
java -jar trak-gui                           # Terminal 2: launch GUI
java -jar trak-cli --remote tasks            # Terminal 3: CLI
```

## Executables

| Executable | Purpose | Default Mode |
|---|---|---|
| `trak-server` | REST API server | Port 8080 |
| `trak-cli` | Command-line client | Local (direct DB) |
| `trak-gui` | Swing desktop client | Remote (needs server) |

```bash
java -jar trak-server [port]              # start server
java -jar trak-cli [--remote] <command>   # CLI
java -jar trak-gui [--local] [--test]     # GUI
```

## Build

Requires Java 17+ and Gradle.

```bash
make build          # build all 3 jars
make build-gui      # build GUI jar only
make build-cli      # build CLI jar only
make build-server   # build server jar only
make test           # run ~200 tests
make clean          # clean artifacts
make reset          # clean + remove .store and .cache
make all            # build + test
make all-test       # build + test + show usage
```

## Makefile Targets

| Target | Description |
|---|---|
| `make build-gui` | Build GUI jar |
| `make build-cli` | Build CLI jar |
| `make build-server` | Build server jar |
| `make reset` | Clean + remove .store and .cache |
| `make cli` | CLI usage help |
| `make cli-test` | Quick CLI test |

## Authentication

A `guest` account (password: `guest`) is created automatically.

```bash
# Interactive login/signup
java -jar trak-cli

# Direct commands
java -jar trak-cli signup manuel --first_name Manuel --last_name Magana --email m@example.com --password pass
java -jar trak-cli login manuel --password pass
java -jar trak-cli logout
```

## Workspace Commands (requires login)

```bash
java -jar trak-cli projects            # list my projects
java -jar trak-cli tasks               # list my tasks
java -jar trak-cli sprints             # list my sprints
java -jar trak-cli detail -p <id>      # project details by ID
java -jar trak-cli detail -t <id>      # task details
java -jar trak-cli detail -s <id>      # sprint details
java -jar trak-cli cur                 # current task + elapsed time
java -jar trak-cli start <task_id>     # start working
java -jar trak-cli end                 # stop working
java -jar trak-cli complete <task_id>  # mark COMPLETE
java -jar trak-cli info                # all commands
```

## Entity CRUD

```bash
# Project (owner defaults to logged-in user)
java -jar trak-cli project add WebApp --summary "Web app"
java -jar trak-cli project get <id>

# Task (--project requires numeric project ID)
java -jar trak-cli task add --title "Fix bug" --project <project_id> --assigned_to manuel --deadline 2026-06-01 --estimate 2h
java -jar trak-cli task update <id> --status INPROGRESS

# Sprint (get requires ID, update with name requires --project)
java -jar trak-cli sprint add Sprint1 --project WebApp
java -jar trak-cli sprint update Sprint1 --project WebApp --start_date 2026-06-01 --end_date 2026-06-14 --add_task <task_id>
```

## GUI Features

- **Dark cinematic theme** — deep charcoal background, warm gold accent, 8px spacing grid
- **Task cards** with gradient backgrounds, gold glow hover, status dropdown (red/amber/green)
- **Delete tasks/projects/sprints** with confirmation dialogs
- **Comprehensive error handling** and input validation on all forms
- **Time input spinners** for estimates (days/hours/minutes) in Add and Edit task dialogs
- **Green CTA button** for Add Task (primary action)
- **Click card** to edit (title, assignee dropdown, status, summary, estimate)
- **Editable tables** for projects and sprints with Save button
- **Double-click cells**: Members (add/remove), Tasks (navigate to filtered view), Sprints (create)
- **Sort** by due date or estimate, **filter** by project
- **Archive** completed tasks (toggle to show/hide)
- **Owner-only** permissions for member/task management
- **Login/Signup/Guest** views, error alert dialogs
- **GlassPanel** rounded containers, **FormPanel** consistent form layout across all dialogs

## Storage

Data persisted in `.store/`. Three formats:

| Format | Config | Files |
|---|---|---|
| **Parquet** (default) | `"parquet"` | `User.parquet`, `Task.parquet`, etc. |
| **JSON** | `"json"` | `user_{name}.json`, `task_{id}.json`, etc. |
| **MongoDB** | `"mongo"` | Collections: `users`, `tasks`, `projects`, `sprints`, `backlogs` |

Configure via `.store/workspace.json`:
```json
{ "store_format": "json" }
```

For MongoDB, set environment variables:
```bash
export MONGO_URI="mongodb+srv://user:pass@cluster.mongodb.net/"
export MONGO_DB="trak"
```

## Architecture

Client-server with clean package boundaries:

- **`task.trak.model`** — shared types: DTOs (`dto/`), request records (`dto/request/`), Session, exceptions (`exception/`), TimeUtil (`util/`)
- **`task.trak.api.service`** — ServiceFactory + service interfaces (only package remaining in `api`)
- **`task.trak.app.server`** — REST API, services, DAO (Parquet/JSON/MongoDB)
- **`task.trak.app.client`** — CLI, HTTP client services (`http/` subpackage)
- **`task.trak.app.client.gui`** — MVC desktop client (communicates via HTTP only, never imports `task.trak.api`)
  - `viewmodel/` — ObservableViewModel, TaskViewModel, ProjectViewModel, SprintViewModel, UserViewModel (Observer pattern)
  - `controller/` — GUIController, AuthController, TaskController, ProjectController, SprintController (receive HTTP services via constructor injection)
  - `view/` — TrakTheme, GlassPanel, TasksView, TaskCardPanel, ProjectsView, SprintView, FormPanel, FormDialogView, TimeInputPanel, ErrorAlertView

GUI uses MVC with an Observer pattern: views implement ViewModelChangeListener and register on ViewModels via `addObserver()`. ViewModels notify registered views on data changes, keeping cross-domain data fresh.

Client never imports from server. GUI communicates exclusively via HTTP services. In `--local` mode, an embedded TrakServer starts on a random port. `ServiceFactory` swaps LOCAL (direct DB) or REMOTE (HTTP) implementations for the CLI.

See [docs/DESIGN.md](docs/DESIGN.md) for full design, [docs/DIAGRAM.md](docs/DIAGRAM.md) for architecture diagrams, [docs/usage.md](docs/usage.md) for detailed usage guide.

## Examples

Example scripts demonstrating the REST API and CLI workflows:

- [`examples/api-demo.sh`](examples/api-demo.sh) — REST API curl demo (authentication, CRUD operations)
- [`examples/cli-demo.sh`](examples/cli-demo.sh) — CLI workflow demo (login, project/task/sprint management)

## Tests

```bash
make test    # ~200 tests across 20+ suites
```
