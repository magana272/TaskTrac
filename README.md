# Trak

A lightweight task and sprint tracking system with CLI, GUI, and REST API. Supports Parquet, JSON, and MongoDB persistence.

## Quick Start

```bash
make build                    # build all executables
make gui-test                 # launch GUI with test data
```

Or start the server and connect clients:

```bash
make server                   # Terminal 1: start REST API
make gui-server               # Terminal 2: launch GUI
java -jar trak-cli --remote tasks   # Terminal 3: CLI
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
make build       # build all 3 jars
make test        # run ~200 tests
make clean       # clean artifacts
make all         # build + test
make all-test    # build + test + show usage
```

## Makefile Targets

| Target | Description |
|---|---|
| `make server` | Start REST API |
| `make gui` | GUI local mode |
| `make gui-test` | GUI local + 20 users, 10 projects, 1000 tasks, 20 sprints |
| `make gui-server` | GUI remote mode |
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

- **Task cards** with status dropdown (red=READY, yellow=INPROGRESS, green=COMPLETE)
- **Click card** to edit (title, assignee dropdown, status, large summary editor)
- **Editable tables** for projects and sprints with Save button
- **Double-click cells**: Members (add/remove), Tasks (add/edit/delete), Sprints (create)
- **Sort** by due date or estimate, **filter** by project
- **Archive** completed tasks (toggle to show/hide)
- **Owner-only** permissions for member/task management
- **Login/Signup/Guest** views (LoginView, SignUpView, LogOutView), error alert dialogs

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

- **`task.trak.api`** — shared DTOs, service interfaces, ServiceFactory
- **`task.trak.app.server`** — REST API, services, DAO (Parquet/JSON/MongoDB)
- **`task.trak.app.client`** — CLI, HTTP client services (`http/` subpackage)
- **`task.trak.app.client.gui`** — MVC desktop client
  - `viewmodel/` — ObservableViewModel, TaskViewModel, ProjectViewModel, SprintViewModel, UserViewModel (Observer pattern)
  - `controller/` — GUIController, AuthController, TaskController, ProjectController, SprintController
  - `view/` — TasksView, ProjectsView, SprintView, form dialogs, ErrorAlertView

GUI uses MVC with an Observer pattern: views implement ViewModelChangeListener and register on ViewModels via `addObserver()`. ViewModels notify registered views on data changes, keeping cross-domain data fresh.

Client never imports from server. `ServiceFactory` swaps LOCAL (direct DB) or REMOTE (HTTP) implementations transparently.

See [docs/DESIGN.md](docs/DESIGN.md) for full design, [docs/DIAGRAM.md](docs/DIAGRAM.md) for architecture diagrams, [docs/usage.md](docs/usage.md) for detailed usage guide.

## Tests

```bash
make test    # ~200 tests across 20+ suites
```
