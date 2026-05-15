# Trak — Diagrams

## Client-Server Architecture

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    secondaryColor: '#f5f5f5'
    tertiaryColor: '#f0f0f0'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
title: Client-Server Architecture
---
flowchart TB
    subgraph Clients
        direction LR
        CLI[CLI Client<br>trak-cli]
        GUI[GUI Client<br>trak-gui]
    end

    subgraph Server[Server - trak-server]
        direction TB
        REST[REST API<br>HttpServer :8080]
        AUTH[AuthFilter<br>Bearer Token]
        SVC[Service Implementations<br>TrakTaskService, ...]
        DAO[DAO Layer<br>EntityDAO]
        subgraph Storage
            direction LR
            PARQUET[(Parquet<br>.store/)]
            JSONDB[(JSON<br>.store/)]
            MONGODB[(MongoDB<br>Atlas)]
            DUCKDB[(DuckDB<br>.store/)]
            REDIS[(Redis<br>:6379)]
        end
        REST --> AUTH --> SVC --> DAO
        DAO --> PARQUET
        DAO --> JSONDB
        DAO --> MONGODB
        DAO --> DUCKDB
        DAO --> REDIS
    end

    CLI -->|"HTTP REST"| REST
    GUI -->|"HTTP REST"| REST
```

## Models

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    secondaryColor: '#f5f5f5'
    tertiaryColor: '#f0f0f0'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
title: Data Model
---
classDiagram
    direction TB

    class STATE {
        <<enumeration>>
        READY
        INPROGRESS
        COMPLETE
    }

    class User {
        -Long id
        -String first_name
        -String last_name
        -String user_name
        -String email
        -String password_hash
        -List~Long~ tasks
        -List~Long~ projects
    }

    class Session {
        -String logged_in_user
        -Long current_task_id
        -Long task_started_at
    }

    class Project {
        -Long id
        -String project_name
        -String summary
        -Date created_at
        -User owner
        -List~User~ members
        -BackLog back_log
        -List~Sprint~ sprints
    }

    class Task {
        -Long id
        -String project_name
        -String assigned_to
        -String title
        -STATE status
        -Date created_at
        -Date completed_at
        -String summary
        -Date deadline
        -String estimate
        -Long time_started
        -Long time_spent_ms
        -Long time_in_ready_ms
        -Long time_in_progress_ms
        -String completion_note
    }

    class Sprint {
        -Long id
        -String project_name
        -String name
        -List~Long~ task_ids
        -Date start_date
        -Date end_date
        -boolean completed
        -Date completed_at
    }

    class BackLog {
        -Long id
        -String name
        -String project_name
        -List~Long~ task_ids
        -Date created_at
    }

    Project "1" *-- "1" User : owner
    Project "1" *-- "0..*" User : members
    Project "1" o-- "1" BackLog : back_log
    Project "1" o-- "0..*" Sprint : sprints
    Task --> STATE : status
    Session --> User : logged_in_user
    Session --> Task : current_task_id
```

## Package Boundaries

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    secondaryColor: '#f5f5f5'
    tertiaryColor: '#f0f0f0'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
    clusterBkg: '#fafafa'
    clusterBorder: '#000000'
title: Package Dependencies
---
flowchart TB
    subgraph api["task.trak.api"]
        direction LR
        sf[ServiceFactory]
        svc_if[service/ interfaces]
    end

    subgraph model["task.trak.model"]
        direction LR
        dto[dto/]
        dto_req[dto/request/]
        exception[exception/]
        util[util/]
        session[Session]
    end

    subgraph client["task.trak.app.client"]
        direction LR
        http[http/<br>ApiClient, TaskHttpService, ...]
        cli[cli/<br>TTApp, CMD_Factory, CMDs]
        viewmodel[gui/viewmodel/<br>TaskViewModel, ProjectViewModel, ...]
        eventbus[gui/viewmodel/event/<br>CommandEventBus]
        controller[gui/controller/<br>GUIController, AuthController, ...]
        views[gui/view/<br>TrakTheme, GlassPanel,<br>task/ +TimeInputPanel, form/ +FormPanel,<br>project/, sprint/, auth/, error/, panel/]
    end

    subgraph server["task.trak.app.server"]
        direction LR
        svc_impl[service/<br>TrakTaskService, ...]
        routes[server/<br>REST Routes]
        dao_layer[dao/<br>EntityDAO, DAOFactory]
        models[model/<br>Task, User, Project, ...]
    end

    client --> model
    http -->|implements service interfaces| api
    server --> api
    server --> model
    client -.-x|never| server

    style client fill:#e3f2fd,stroke:#1565c0
    style server fill:#fce4ec,stroke:#c62828
    style api fill:#f1f8e9,stroke:#558b2f
    style model fill:#fff3e0,stroke:#e65100
```

## ServiceFactory Flow

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
title: ServiceFactory — Mode Switching
---
flowchart LR
    CMD[CMD Class] -->|ServiceFactory.taskService| SF{ServiceFactory}
    SF -->|LOCAL| Direct[TrakTaskService<br>→ DAO → Files]
    SF -->|REMOTE| HTTP[TaskHttpService<br>→ HTTP → Server]

    style SF fill:#fff3e0,stroke:#e65100
    style Direct fill:#f1f8e9,stroke:#558b2f
    style HTTP fill:#e3f2fd,stroke:#1565c0
```

## AuthFilter

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
title: AuthFilter — Request Authentication
---
flowchart LR
    Req[Request] --> AF[AuthFilter]
    AF -->|valid Bearer token| Handler[Handler processes request]
    AF -->|missing / invalid token| Reject[401 Authentication required]

    style AF fill:#fff3e0,stroke:#e65100
    style Handler fill:#f1f8e9,stroke:#558b2f
    style Reject fill:#fce4ec,stroke:#c62828
```

## GUI MVC Observer Pattern

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
title: GUI MVC with Observer Pattern
---
flowchart TB
    User[User Action] --> View[View<br>e.g. TasksView]
    View -->|calls| Controller[Controller<br>e.g. TaskController]
    Controller -->|invokes| Service[Service Layer]
    Service -->|returns data| Controller
    Controller -->|updates| ViewModel[ViewModel<br>e.g. TaskViewModel]
    ViewModel -->|notifyObservers| View
    View -->|render| UI[Updated UI]

    subgraph Observer Registration
        direction LR
        TasksView_Obs[TasksView] -->|addObserver| TaskVM[TaskViewModel]
        TasksView_Obs -->|addObserver| ProjVM1[ProjectViewModel]
        ProjectsView_Obs[ProjectsView] -->|addObserver| ProjVM2[ProjectViewModel]
        SprintView_Obs[SprintView] -->|addObserver| SprintVM[SprintViewModel]
        SprintView_Obs -->|addObserver| ProjVM3[ProjectViewModel]
        SprintView_Obs -->|addObserver| TaskVM2[TaskViewModel]
    end

    style View fill:#e3f2fd,stroke:#1565c0
    style Controller fill:#fff3e0,stroke:#e65100
    style ViewModel fill:#f1f8e9,stroke:#558b2f
```

## Theme System

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
title: Theme System
---
flowchart TB
    GUIMain[GUIMain] -->|"1. setCrossPlatformLookAndFeel()"| UIManager[UIManager Defaults]
    GUIMain -->|"2. applyDefaults()"| TrakTheme
    TrakTheme -->|"sets ~50 UIManager keys"| UIManager
    UIManager -->|"inherited by"| AllComponents[All Swing Components]

    subgraph TrakTheme["TrakTheme Constants"]
        direction LR
        Colors["Colors<br>BG_DARK, ACCENT,<br>STATUS_*"]
        Fonts["Typography<br>DISPLAY → CAPTION<br>+ MONO"]
        Spacing["Spacing Grid<br>SP_XS(4) → SP_3XL(48)"]
    end

    subgraph Methods["Styling Methods"]
        direction LR
        BtnStyles["styleButtonPrimary<br>styleButtonNav<br>styleButtonAccent"]
        TableStyles["styleTable<br>styleComboBox<br>styleStatusComboBox"]
        CardStyles["cardBorder<br>cardBorderHover<br>statusColor"]
    end

    TrakTheme --> Methods

    subgraph CustomPanels["Custom Panels"]
        GlassPanel["GlassPanel<br>rounded gradient + shadow"]
        FormPanel["FormPanel<br>two-column GridBag"]
    end

    style TrakTheme fill:#fff3e0,stroke:#e65100
    style UIManager fill:#f1f8e9,stroke:#558b2f
    style Methods fill:#e3f2fd,stroke:#1565c0
    style CustomPanels fill:#fce4ec,stroke:#c62828
```

## Command Routing

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    secondaryColor: '#f5f5f5'
    tertiaryColor: '#f0f0f0'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '13px'
    clusterBkg: '#fafafa'
    clusterBorder: '#000000'
title: CLI Commands
---
flowchart LR
    CLI[CLI Input] --> F[CMD_Factory]

    F --> Auth
    F --> Work
    F --> CRUD

    subgraph Auth
        login
        signup
        logout
    end

    subgraph Work[Workspace]
        direction LR
        projects --- tasks --- sprints --- detail
        cur --- start --- endc[end] --- complete
        addtask --- addmember --- sprintplan --- info
    end

    subgraph CRUD[Entity CRUD]
        direction LR
        user --- project --- task --- sprint --- backlog
    end
```

## REST API

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '13px'
title: REST API Endpoints
---
flowchart TB
    Server[HttpServer :8080]

    subgraph Public["Public (no auth)"]
        Auth["/api/auth/login\n/api/auth/signup\n/api/auth/logout\nPOST"]
        UserCreate["/api/users\nPOST"]
    end

    subgraph Protected["Protected (Bearer token)"]
        Users["/api/users/{username}\nGET PUT DELETE"]

        subgraph ProjectRoutes[Projects]
            ProjList["/api/projects\nGET POST\n?user="]
            ProjById["/api/projects/id/{id}\nGET"]
            ProjByName["/api/projects/name/{name}\nGET"]
            ProjDetail["/api/projects/{name}\nPUT DELETE"]
            ProjMembers["/api/projects/{name}/members\nPOST"]
        end

        Tasks["/api/tasks\nGET POST ?assignee=\n/api/tasks/{id}\nGET PUT DELETE"]

        subgraph SprintRoutes[Sprints]
            SprintList["/api/sprints\nGET POST"]
            SprintDetail["/api/sprints/{id}\nGET PUT DELETE"]
            SprintByName["/api/sprints/name/{name}\nGET ?project="]
        end

        Backlogs["/api/backlogs/{name}\nGET POST PUT DELETE"]
    end

    Server --> Public
    Server --> Protected
    Auth -.->|token| SessionMgr[SessionManager<br>UUID → username]
    SessionMgr -.->|validates| Protected

    style Public fill:#f1f8e9,stroke:#558b2f
    style Protected fill:#fff3e0,stroke:#e65100
```

## Storage Backends

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
title: DAO Factory — Storage Format Selection
---
flowchart LR
    DAO[DAOFactory] -->|PARQUET| PQ[ParquetXxxDAO<br>Avro + Snappy<br>.store/*.parquet]
    DAO -->|JSON| JS[JsonXxxDAO<br>Gson<br>.store/*.json]
    DAO -->|MONGO| MG[MongoXxxDAO<br>MongoDB Driver<br>Atlas / local]
    DAO -->|"DUCKDB (default)"| DK[DuckDB*DAO<br>JDBC embedded<br>.store/trak.duckdb]
    DAO -->|REDIS| RD[RedisDAO<br>Jedis<br>localhost:6379]

    Config[workspace.json<br>store_format] -.-> DAO
    ENV[MONGO_URI<br>MONGO_DB] -.-> MG
    RENV[REDIS_URL] -.-> RD

    style PQ fill:#e8eaf6,stroke:#283593
    style JS fill:#f1f8e9,stroke:#558b2f
    style MG fill:#fce4ec,stroke:#c62828
    style DK fill:#fff8e1,stroke:#f57f17
    style RD fill:#e0f2f1,stroke:#00695c
```

## Sprint Identity

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
title: Same Sprint Name, Different Projects
---
flowchart TB
    S1["Sprint1 — ProjectA"] --> T1[Task A1]
    S1 --> T2[Task A2]
    S2["Sprint1 — ProjectB"] --> T3[Task B1]

    style S1 fill:#f5f5f5,stroke:#000,stroke-width:2px
    style S2 fill:#f0f0f0,stroke:#000,stroke-width:2px
```

Sprints are keyed by auto-generated ID, not by name.
Two projects can each have a sprint named "Sprint1".
Storage format depends on the configured backend (see Storage Backends diagram).

## Local vs Remote Mode

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
title: Operational Modes
---
flowchart TB
    subgraph RemoteMode["Remote Mode (default for GUI)"]
        direction TB
        GUI_R[trak-gui] -->|HTTP| ExtServer[trak-server :8080]
        CLI_R[trak-cli --remote] -->|HTTP| ExtServer
        ExtServer --> SVC_R[Services] --> DAO_R[DAO] --> Store_R[(Storage)]
    end

    subgraph LocalGUI["Local Mode (--local flag)"]
        direction TB
        GUI_L[trak-gui --local] -->|"starts embedded"| EmbServer["TrakServer\nport 0 (auto)"]
        GUI_L -->|"HTTP to localhost:{port}"| EmbServer
        EmbServer --> SVC_L[Services] --> DAO_L[DAO] --> Store_L[(Storage)]
    end

    subgraph LocalCLI["Local Mode (default for CLI)"]
        direction TB
        CLI_L[trak-cli] --> SF[ServiceFactory\nregisterLocalServices]
        SF --> SVC_D[TrakTaskService, ...] --> DAO_D[DAO] --> Store_D[(Storage)]
    end

    style RemoteMode fill:#e3f2fd,stroke:#1565c0
    style LocalGUI fill:#fff3e0,stroke:#e65100
    style LocalCLI fill:#f1f8e9,stroke:#558b2f
```

The GUI always communicates via HTTP — even in `--local` mode, it starts an embedded server on a random port and connects to it. The CLI in local mode bypasses HTTP entirely and calls services directly via `ServiceFactory`.

## DTO & Request/Response Flow

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
title: DTO & Request/Response Flow
---
flowchart LR
    subgraph Client
        ReqDTO["CreateTaskRequest\nUpdateTaskRequest\n+ validate()"]
    end

    subgraph Server
        Route[Route Handler] --> Validate["validate()"]
        Validate --> Service[Service Layer]
        Service --> Model["Server Model\nTask, User, Project,\nSprint, BackLog"]
        Model --> Convert["→ toDTO()"]
    end

    subgraph Response
        RespDTO["TaskDTO\nUserDTO\nProjectDTO\nSprintDTO\nBacklogDTO"]
    end

    ReqDTO -->|"JSON POST/PUT"| Route
    Convert --> RespDTO
    RespDTO -->|"JSON response"| Client

    style Client fill:#e3f2fd,stroke:#1565c0
    style Server fill:#fff3e0,stroke:#e65100
    style Response fill:#f1f8e9,stroke:#558b2f
```

Request DTOs are Java records with a `validate()` method that throws `ValidationException` on invalid input. Server models are internal — only DTOs cross the HTTP boundary.

## Exception Hierarchy

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: '#ffffff'
    primaryTextColor: '#000000'
    primaryBorderColor: '#000000'
    lineColor: '#000000'
    fontFamily: 'Georgia, Times New Roman, serif'
    fontSize: '14px'
title: Exception Hierarchy
---
classDiagram
    direction TB

    class RuntimeException {
        <<Java stdlib>>
    }

    class TrakException {
        +String message
        +Throwable cause
    }

    class ValidationException {
        bad input or constraint
    }

    class EntityNotFoundException {
        missing user, task, project, sprint
    }

    class AuthenticationException {
        bad credentials or expired token
    }

    class DuplicateEntityException {
        username or email already exists
    }

    RuntimeException <|-- TrakException
    TrakException <|-- ValidationException
    TrakException <|-- EntityNotFoundException
    TrakException <|-- AuthenticationException
    TrakException <|-- DuplicateEntityException

    style TrakException fill:#fff3e0,stroke:#e65100
    style ValidationException fill:#e3f2fd,stroke:#1565c0
    style EntityNotFoundException fill:#e3f2fd,stroke:#1565c0
    style AuthenticationException fill:#fce4ec,stroke:#c62828
    style DuplicateEntityException fill:#e3f2fd,stroke:#1565c0
```

All exceptions are unchecked (`RuntimeException`). Route handlers catch `TrakException` subclasses and map them to HTTP status codes (400, 401, 404, 409).
