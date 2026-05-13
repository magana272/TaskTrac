# Trak Usage Guide

## Setup

Build all executables:

```bash
make build
```

This produces three jar files:

| Executable | Purpose |
|---|---|
| `trak-server` | REST API server |
| `trak-cli` | Command-line client |
| `trak-gui` | Swing desktop client |

---

## Quick Start

### Option 1: GUI with test data (fastest)
```bash
make gui-test
```
Launches the GUI pre-loaded with 20 users, 10 projects, 1000 tasks, and 20 sprints. Logged in as `guest`.

### Option 2: GUI standalone
```bash
make gui
```
Launches the GUI in local mode. Click "Continue as Guest" or sign up.

### Option 3: CLI
```bash
java -jar trak-cli
# Interactive prompt — login as guest (password: guest) or create account
```

### Option 4: Client-Server mode
```bash
# Terminal 1: start server
make server

# Terminal 2: GUI connecting to server
make gui-server

# Terminal 3: CLI connecting to server
java -jar trak-cli --remote tasks
```

---

## Makefile Targets

| Target | Description |
|---|---|
| `make build` | Build all 3 jars |
| `make test` | Run all tests |
| `make clean` | Clean build artifacts |
| `make server` | Start REST API server (port 8080) |
| `make gui` | GUI in local mode |
| `make gui-test` | GUI in local mode with test data |
| `make gui-server` | GUI connecting to server |
| `make gui-test-server` | GUI connecting to server with test data |
| `make cli` | Show CLI usage |
| `make cli-test` | Quick CLI test (runs `info`) |
| `make all` | Build + run tests |
| `make all-test` | Build + run tests + show usage |

---

## CLI Usage

### Authentication

```bash
# Interactive login/signup
java -jar trak-cli

# Direct login
java -jar trak-cli login manuel --password mypassword

# Create account
java -jar trak-cli signup manuel --first_name Manuel --last_name Magana --email manuel@example.com --password mypassword

# Logout
java -jar trak-cli logout
```

A `guest` account (password: `guest`) is created automatically on first run.

### Workspace Commands (requires login)

```bash
# List my projects (table: ID, Name, Description, Contact)
java -jar trak-cli projects

# List my tasks (table: ID, Project, Sprint, Status, Name, Summary, Deadline)
java -jar trak-cli tasks

# List my sprints
java -jar trak-cli sprints

# Full details by type and ID
java -jar trak-cli detail -p <project_id>
java -jar trak-cli detail -t <task_id>
java -jar trak-cli detail -s <sprint_id>

# Current task and elapsed time
java -jar trak-cli cur

# Start/stop working on a task (tracks time)
java -jar trak-cli start <task_id>
java -jar trak-cli end

# Mark task as COMPLETE
java -jar trak-cli complete <task_id>

# Add member to project
java -jar trak-cli addmember MobileApp alice

# Interactive task creation
java -jar trak-cli addtask MobileApp

# Interactive sprint planning
java -jar trak-cli sprintplan

# List all commands
java -jar trak-cli info
```

### User Management

```bash
java -jar trak-cli user add jdoe --first_name Jane --last_name Doe --email jane@example.com --password secret
java -jar trak-cli user get jdoe
java -jar trak-cli user update jdoe --email newemail@example.com --password newsecret
java -jar trak-cli user delete jdoe
```

### Project Management

Projects require an owner (defaults to logged-in user). Members referenced by username.

```bash
java -jar trak-cli project add MyProject --summary "Project description" --members [alice,bob]
java -jar trak-cli project get <project_id>
java -jar trak-cli project update MyProject --summary "Updated description"
java -jar trak-cli project delete MyProject
```

### Task Management

Tasks require a project ID (numeric). Auto-generated task IDs.

```bash
java -jar trak-cli task add --title "Fix login bug" --project <project_id> --assigned_to jdoe --summary "Users cannot log in" --deadline 2026-06-01 --estimate 4h
java -jar trak-cli task get <task_id>
java -jar trak-cli task update <task_id> --status INPROGRESS
java -jar trak-cli task delete <task_id>
```

Task statuses: `READY` (default), `INPROGRESS`, `COMPLETE`.

### Sprint Management

Sprint `get` requires numeric ID. Sprint `update` with name requires `--project` to disambiguate.

```bash
java -jar trak-cli sprint add Sprint1 --project MobileApp
java -jar trak-cli sprint get <sprint_id>
java -jar trak-cli sprint update Sprint1 --project MobileApp --start_date 2026-06-01 --end_date 2026-06-14
java -jar trak-cli sprint update Sprint1 --project MobileApp --add_task <task_id>
java -jar trak-cli sprint delete Sprint1
```

### Backlog Management

```bash
java -jar trak-cli backlog add MainBacklog --project MyProject
java -jar trak-cli backlog get MainBacklog
java -jar trak-cli backlog update MainBacklog --add_task <task_id>
java -jar trak-cli backlog update MainBacklog --remove_task <task_id>
java -jar trak-cli backlog delete MainBacklog
```

---

## GUI Features

### Task View
- **Task cards** with title, status dropdown, project name, summary, deadline
- **Status colors**: READY (red), INPROGRESS (yellow), COMPLETE (green)
- **Click card** to open edit dialog (title, assigned to, summary, status)
- **Status dropdown** on card changes status immediately
- **Sort** by Due Date or Estimate
- **Filter** by Project
- **Archive toggle** hides completed tasks (shows count)
- **"+" button** to add new task (project dropdown, assignee from members, date picker)

### Project View
- **Editable table** — click Name or Description to edit inline, Save Changes button
- **Double-click Members cell** — manage members dialog (add/remove, owner only)
- **Double-click Tasks cell** — task manager dialog (edit/delete tasks, owner only)
- **Double-click Sprints cell** — create sprint dialog with date pickers
- **Double-click Description cell** — large text editor dialog
- **"+ Add Project"** button

### Sprint View
- **Editable table** — edit Start Date and End Date inline, Save Changes button
- **Double-click Tasks cell** — view sprint tasks with edit buttons
- **"+ Add Sprint"** button with project dropdown, date pickers, task checklist

### Status Bar
- Shows logged-in user
- **Login/Signup** buttons with form dialogs
- **Continue as Guest** button (logs in as `guest` account)
- **Logout** button (visible when logged in)

### Error Panel
- Red dismissable bar below nav for errors
- Auto-hides after 8 seconds

---

## Server & REST API

### Start the server
```bash
java -jar trak-server          # default port 8080
java -jar trak-server 9090     # custom port
```

### Connect clients
```bash
# CLI
java -jar trak-cli --remote tasks
java -jar trak-cli --remote --server-url http://localhost:9090 tasks

# GUI
java -jar trak-gui                                        # default localhost:8080
java -jar trak-gui --server-url http://myserver:8080
```

### API Endpoints

All endpoints return JSON. Auth required via `Authorization: Bearer <token>` header.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Login → `{token, username}` |
| POST | `/api/auth/signup` | Signup → `{token, username}` |
| POST | `/api/auth/logout` | Logout |
| GET/POST/PUT/DELETE | `/api/users/{username}` | User CRUD |
| GET | `/api/projects?user={username}` | List projects by user |
| GET | `/api/projects/id/{id}` | Get project by ID |
| GET | `/api/projects/name/{name}` | Get project by name |
| POST | `/api/projects` | Create project |
| PUT/DELETE | `/api/projects/{name}` | Update/delete project |
| POST | `/api/projects/{name}/members` | Add member |
| GET | `/api/tasks?assignee={username}` | List tasks by assignee |
| GET/POST/PUT/DELETE | `/api/tasks/{id}` | Task CRUD |
| GET/POST | `/api/sprints` | List/create sprints |
| GET/PUT/DELETE | `/api/sprints/{id}` | Sprint CRUD |
| GET/POST/PUT/DELETE | `/api/backlogs/{name}` | Backlog CRUD |

---

## Data Storage

Three persistence formats (configurable via `.store/workspace.json`):

| Format | Config Value | Storage |
|---|---|---|
| **Parquet** (default) | `"parquet"` | `.store/User.parquet`, `.store/Task.parquet`, etc. |
| **JSON** | `"json"` | `.store/user_{name}.json`, `.store/task_{id}.json`, etc. |
| **MongoDB** | `"mongo"` | Collections: `users`, `tasks`, `projects`, `sprints`, `backlogs` |

Always JSON regardless of format: `session.json` (login state), `workspace.json` (config).

### Switch to JSON
```json
{ "store_format": "json" }
```

### Switch to MongoDB
```json
{ "store_format": "mongo" }
```

Set environment variables:
```bash
export MONGO_URI="mongodb+srv://user:pass@cluster.mongodb.net/"
export MONGO_DB="trak"    # optional, defaults to "trak"
```

---

## Running Tests

```bash
make test
```

138 tests covering: authentication, user/project/task/sprint/backlog CRUD, workspace commands, detail flags, password hashing, session persistence, service list operations, seed data generation.
