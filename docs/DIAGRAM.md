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

    subgraph Shared[API Package]
        DTOs[DTOs<br>TaskDTO, ProjectDTO, ...]
        Interfaces[Service Interfaces<br>TaskService, ProjectService, ...]
        SF[ServiceFactory]
    end

    subgraph Server[Server - trak-server]
        direction TB
        REST[REST API<br>HttpServer :8080]
        SVC[Service Implementations<br>TrakTaskService, ...]
        DAO[DAO Layer<br>EntityDAO]
        subgraph Storage
            direction LR
            PARQUET[(Parquet<br>.store/)]
            JSONDB[(JSON<br>.store/)]
            MONGODB[(MongoDB<br>Atlas)]
        end
        REST --> SVC --> DAO
        DAO --> PARQUET
        DAO --> JSONDB
        DAO --> MONGODB
    end

    CLI -->|HTTP or Local| SF
    GUI -->|HTTP or Local| SF
    SF -->|REMOTE mode| REST
    SF -->|LOCAL mode| SVC
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
    subgraph api["org.trak.api (shared)"]
        direction LR
        dto[dto/]
        svc_if[service/ interfaces]
        session[model/ Session]
        util[util/]
    end

    subgraph client["org.trak.app.client"]
        direction LR
        http[HTTP Services<br>TaskHttpService, ...]
        cli[cli/<br>TTApp, CMD_Factory, CMDs]
        gui[gui/<br>TTAppGUI, MainFrame, Cards]
        observer[gui/observer/<br>CommandEventBus]
    end

    subgraph server["org.trak.app.server"]
        direction LR
        svc_impl[service/<br>TrakTaskService, ...]
        routes[server/<br>REST Routes]
        dao_layer[dao/<br>EntityDAO, DAOFactory]
        models[model/<br>Task, User, Project, ...]
    end

    client --> api
    server --> api
    client -.-x|never| server

    style client fill:#e3f2fd,stroke:#1565c0
    style server fill:#fce4ec,stroke:#c62828
    style api fill:#f1f8e9,stroke:#558b2f
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

## GUI Observer Pattern

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
title: GUI Event Flow
---
flowchart LR
    Input[User Input<br>Button / Command] --> TTAppGUI
    TTAppGUI -->|background thread| CMDFactory[CMD_Factory]
    CMDFactory --> CMD[Command]
    CMD -->|stdout captured| TeeOS[TeeOutputStream]
    CMDFactory -->|fire| EventBus[CommandEventBus]
    EventBus -->|notify| TTAppGUI
    TTAppGUI -->|EDT| MainFrame
    MainFrame --> Cards[Task Cards]
    MainFrame --> Tables[Project/Sprint Tables]
    MainFrame --> Error[Error Panel]
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
