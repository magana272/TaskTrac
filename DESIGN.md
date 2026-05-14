# DESIGN: feature/MainView-UXUI

## Goal

Establish a polished dark cinematic UI for the Trak GUI, add workspace scoping (Mine vs Team), replace free-text estimate input with structured duration spinners, and style the primary Add Task button as a green CTA. Collectively, these features transform the GUI from a functional prototype into a production-quality desktop application.

## Success Criteria

- Dark cinematic theme applied globally via `TrakTheme.applyDefaults()`
- All colors/fonts/spacing drawn from `TrakTheme` constants — no hardcoded literals in views
- 8px spacing grid enforced across all panels
- Workspace toggle: Mine shows user-scoped data, Team shows all data
- Toggle state affects Tasks and Projects views via `refreshTasks(bool)` / `refreshProjects(bool)`
- TimeInputPanel: days [0–30], hours [0–24], minutes [0–59] spinners
- Estimate editing supported in TaskEditView (previously missing)
- Green CTA for Add Task button using `TrakTheme.styleButtonPrimary()`
- Task cards redesigned with gradient backgrounds, gold glow hover, status-colored combo
- `FormPanel` used by all form dialogs for consistent two-column grid layout
- `GlassPanel` provides glassmorphism container for elevated panels

## Architecture Decisions

### Feature 1: Dark Cinematic Theme

#### New file: `TrakTheme.java`
- Package: `task.trak.app.client.gui.view`
- Final class with static constants and static utility methods
- Color palette: `BG_DARK` (#121216), `BG_SURFACE`, `BG_ELEVATED`, `BG_INPUT`
- Accent: `ACCENT` (#FFD54F warm gold), `ACCENT_GREEN` (#34C759), `ACCENT_BLUE`
- Status colors: `STATUS_READY` (red), `STATUS_INPROGRESS` (amber), `STATUS_COMPLETE` (green)
- Typography scale: `FONT_DISPLAY` (22pt bold) → `FONT_TITLE` → `FONT_HEADING` → `FONT_BODY` → `FONT_SMALL` → `FONT_CAPTION` (10pt) + `FONT_MONO`
- Spacing scale: `SP_XS`(4) through `SP_3XL`(48) on 8px grid
- `applyDefaults()` sets ~50 UIManager keys (Panel, Label, TextField, PasswordField, TextArea, ComboBox, Button, Table, ScrollBar, etc.)
- Styling methods: `styleButtonPrimary()`, `styleButtonNav()`, `styleButtonAccent()`, `styleComboBox()`, `styleStatusComboBox()`, `styleTable()`, `cardBorder()`, `cardBorderHover()`, `statusColor()`, `pad()`

#### New file: `GlassPanel.java`
- Package: `task.trak.app.client.gui.view`
- Extends `JPanel`, custom `paintComponent` with `RoundRectangle2D`
- Configurable corner radius, optional drop shadow, gradient fill (top/bottom)
- Glass reflection highlight (top 1/3, 8-alpha white)
- Subtle 12-alpha white border stroke

#### New file: `FormPanel.java`
- Package: `task.trak.app.client.gui.view.form`
- Extends `JPanel` with `GridBagLayout`
- `addField(label, component)` — label in col 0, component stretches in col 1
- `addExpandingField(label, component)` — same but with `BOTH` fill + weighty for text areas
- Used by all 8 form views (LoginView, SignUpView, LogOutView, ProjectAddView, ProjectCreateView, SprintAddView, TaskAddView, TaskEditView)

#### Modified: `FormDialogView.java`
- `buildPanel()` return type changed from `JPanel` to `FormPanel`

#### Modified: `GUIMain.java`
- Sets cross-platform L&F (`UIManager.getCrossPlatformLookAndFeelClassName()`) instead of system L&F
- Calls `TrakTheme.applyDefaults()` before creating any Swing components

#### Modified: `MainFrame.java`, `StatusPanel.java`, `CommandInputPanel.java`, `OutputPanel.java`
- All use `TrakTheme` constants for colors, fonts, borders, spacing
- `StatusPanel` redesigned: "TRAK" gold branding, status dot indicator, divider, restyled auth buttons

#### Modified: All form views
- Return `FormPanel` from `buildPanel()` instead of `JPanel`
- Use `FormPanel.addField()` for consistent layout

### Feature 2: Workspace Toggle (Mine/Team)

#### Modified: `MainFrame.java`
- Added `myWorkspaceBtn` ("⌂ Mine") and `teamWorkspaceBtn` ("✳ Team")
- `styleWorkspaceBtn()` toggles gold active vs muted inactive
- `setTeamMode(boolean)` calls `refreshTasks(team)`, `refreshProjects(team)`, `refreshSprints()`
- Nav button click handlers pass `teamMode` to refresh methods
- `teamMode` field tracks current state, resets to `false` on logout

#### Modified: `TaskController.java`
- `refreshTasks(boolean teamMode)` — `true` calls `listAll()`, `false` calls `listByAssignee()`
- `refreshTasks()` convenience overload defaults to `false`

#### Modified: `ProjectController.java`
- `refreshProjects(boolean teamMode)` — `true` calls `listAll()`, `false` calls `listByUser()`
- `refreshProjects()` convenience overload defaults to `false`

### Feature 3: TimeInput (Duration Spinners)

#### New file: `TimeInputPanel.java`
- Package: `task.trak.app.client.gui.view.task`
- Extends `JPanel` — reusable inline component
- Three `JSpinner` fields with `SpinnerNumberModel` for days/hours/minutes
- `getDurationString()` → returns `"Xd Yh Zm"` format string
- `setDuration(String)` → parses existing estimate string into spinner values
- `isZero()` → validation check

#### Modified: `TimeUtil.java`
- `parseDurationToComponents(String)` → returns `int[] {days, hours, minutes}`
- Parses format like `"2d 5h 30m"`, `"5h"`, `"30m"`, etc.
- `parseDurationToMs(String)` → converts to milliseconds

#### Modified: `TaskAddView.java`
- Replace `JTextField estimateField` with `TimeInputPanel estimatePanel`
- Validation: reject if `estimatePanel.isZero()`
- `onConfirm()`: pass `estimatePanel.getDurationString()` as estimate

#### Modified: `TaskEditView.java`
- Add `TimeInputPanel estimatePanel` pre-populated from `task.estimate()`
- Add estimate to change detection in `onConfirm()`
- Pass estimate through to controller

#### Modified: `TaskController.java`
- `updateTask()` gains `String estimate` parameter

#### Modified: `TaskService.java` (interface), `TrakTaskService.java` (server impl), `TaskHttpService.java` (HTTP impl)
- `updateById()` gains `String newEstimate` parameter
- CLI commands pass `null` for backward compatibility

### Feature 4: Green CTA + Task Card Redesign

#### Modified: `TaskCardPanel.java`
- Custom `paintComponent`: rounded corners, gradient background, gold glow on hover
- Uses TrakTheme colors throughout (`CARD_BG`, `CARD_HOVER_BG`, `BORDER`, `BORDER_HOVER`, etc.)
- Status combo styled via `TrakTheme.styleStatusComboBox()`

#### Modified: `TasksView.java`
- Add Task buttons styled via `TrakTheme.styleButtonPrimary()` (green CTA)

## UI/UX Approach

```
┌───────────────────────────────────────────────────────────┐
│  TRAK  │  ● user        [Login] [Sign Up] [Guest]        │  StatusPanel
├──────────────────────────────────────────────────────────-─┤
│  Projects  Tasks  Sprints              ⌂ Mine  ✳ Team    │  NavBar
├───────────────────────────────────────────────────────────┤
│                                                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │
│  │ Task Card   │  │ Task Card   │  │ Task Card   │      │  TasksView
│  │ gradient bg │  │ gold glow   │  │ status color│      │  (card grid)
│  └─────────────┘  └─────────────┘  └─────────────┘      │
│                                                           │
├───────────────────────────────────────────────────────────┤
│  > command input                                   [Run]  │  CommandInputPanel
└───────────────────────────────────────────────────────────┘
```

- Deep charcoal canvas (`#121216`) with layered depth via `BG_SURFACE` and `BG_ELEVATED`
- Warm gold accent (`#FFD54F`) for branding, active nav, and accent buttons
- 8px spacing grid for consistent whitespace
- Typography scale from `DISPLAY` (hero branding) to `CAPTION` (muted metadata)
- `GlassPanel` for elevated card containers with rounded corners and glass reflection
- Editorial header: "TRAK" gold branding + status dot + divider + user label
- Workspace toggle in nav bar: gold active state, muted inactive state

## Data Flow

```
Workspace toggle:
  Button click → setTeamMode(bool) → refreshTasks(bool) / refreshProjects(bool)
    → service.listAll() or service.listByAssignee/listByUser()
    → viewModel.setAll() → notifyObservers → views re-render

TimeInput:
  TimeInputPanel.getDurationString()  →  "2d 5h 30m"
      ↓
  TaskController.addTask/updateTask(... estimate)
      ↓
  TaskService.create/updateById(... estimate)
      ↓
  Task.setEstimate("2d 5h 30m")  →  persisted as String

Theme:
  GUIMain → setCrossPlatformLookAndFeel()
         → TrakTheme.applyDefaults() → UIManager defaults
         → all Swing components inherit dark theme
```

## State Management

- No new ViewModel classes — estimate is already on `TaskDTO`
- `teamMode` boolean tracked in `MainFrame`, resets on logout
- Theme is static — applied once at startup via `TrakTheme.applyDefaults()`

## Testing Strategy

- Unit test `TimeUtil.parseDurationToComponents()` for various formats
- Unit test `TimeInputPanel.isZero()` validation
- Cucumber scenario: add task with estimate, verify estimate persists
- Visual QA: verify dark theme renders correctly on all views
- Workspace toggle: verify Mine shows filtered data, Team shows all data

## Tradeoffs

- **Cross-platform L&F vs system L&F**: Chose cross-platform for full color control — system L&F on macOS ignores many UIManager color overrides
- **TrakTheme as static constants vs properties file**: Constants for compile-time safety and IDE autocomplete; no runtime theme switching needed
- **Storing estimate as formatted String vs millis**: Keeping String for backward compatibility with existing data. Format is human-readable and parseable
- **Max 30 days estimate upper bound**: Reasonable for task estimates; can be adjusted later

## Risks

- Cross-platform L&F renders slightly differently on Linux/Windows vs macOS
- Existing tasks with free-text estimates (e.g., "about 2 hours") won't parse cleanly into the spinners — `setDuration()` defaults to 0/0/0 for unparseable strings
- Custom `paintComponent` in `TaskCardPanel` and `GlassPanel` adds rendering overhead for large task lists
