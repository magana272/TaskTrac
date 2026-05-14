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
        end
        REST --> AUTH --> SVC --> DAO
        DAO --> PARQUET
        DAO --> JSONDB
        DAO --> MONGODB
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
    }

    class Sprint {
        -Long id
        -String project_name
        -String name
        -List~Long~ task_ids
        -Date start_date
        -Date end_date
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
    Server --> Auth[/api/auth/<br>POST login, signup, logout/]
    Server --> Users[/api/users/<br>GET POST PUT DELETE/]
    Server --> Projects[/api/projects/<br>GET POST PUT DELETE<br>?user= filter/]
    Server --> Tasks[/api/tasks/<br>GET POST PUT DELETE<br>?assignee= filter/]
    Server --> Sprints[/api/sprints/<br>GET POST PUT DELETE/]
    Server --> Backlogs[/api/backlogs/<br>GET POST PUT DELETE/]

    Auth -.->|token| SessionMgr[SessionManager<br>UUID → username]
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

    Config[workspace.json<br>store_format] -.-> DAO
    ENV[MONGO_URI<br>MONGO_DB] -.-> MG

    style PQ fill:#e8eaf6,stroke:#283593
    style JS fill:#f1f8e9,stroke:#558b2f
    style MG fill:#fce4ec,stroke:#c62828
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
Stored as `sprint_{id}.json`.
