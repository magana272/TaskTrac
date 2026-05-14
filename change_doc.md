# Change Documentation

## bugfix/observer-exception-isolation

### Design Changes
- **File**: `ObservableViewModel.java` — `notifyObservers()` method
- **Problem**: One throwing observer prevents all subsequent observers from receiving notifications. The `CopyOnWriteArrayList` iteration propagates exceptions from any listener, breaking the notification chain.
- **Solution**: Wrap each `listener.onViewModelChanged(type)` call in a try-catch block. Exceptions are logged to stderr but do not interrupt notification of remaining observers.
- **Impact**: All existing observers continue to work. Faulty observers are isolated — they fail silently (with a log) instead of breaking the entire notification chain.

## bugfix/logout-card-visibility

### Design Changes
- **Files**: `MainFrame.java`, `GUIController.java`, `ObservableViewModel.java`
- **Problem**: After logout, `clearViewModels()` fires TASKS/PROJECTS/SPRINTS notifications. `MainFrame.onViewModelChanged` processes these and switches the CardLayout to show task/project/sprint views — overriding the CARD_OUTPUT (guest) view that the SESSION handler just set. User ends up seeing an empty data view with interactive elements instead of the output panel.
- **Solution**:
  1. Guard TASKS/PROJECTS/SPRINTS handlers in `MainFrame.onViewModelChanged` — return early if session is null
  2. Disable nav buttons (Tasks, Projects, Sprints) when logged out; re-enable on login
  3. Remove redundant `clearViewModels()` from `GUIController.onCommandExecuted(LOGOUT)` — the MainFrame SESSION handler already calls it
  4. Add `clearCache()` to `ObservableViewModel` — deletes `.cache/*.ser` files on logout
  5. Call `clearCache()` in `GUIController.clearViewModels()`
- **Impact**: After logout, user sees only the output panel. Nav buttons are disabled. Cache files are cleaned up. No stale data persists.

## bugfix/task-assignee-selection

### Design Changes
- **File**: `ProjectController.java` — `getProjectMembers()` method
- **Problem**: `getProjectMembers()` returns `project.memberUsernames()` which excludes the project owner. The `TaskCardPanel` edit dialog uses this list for its assignee dropdown, so the owner is never listed as a selectable assignee.
- **Solution**: Rebuild the list in `getProjectMembers()` to include the owner first, followed by non-owner members (deduplicating if owner appears in both). This matches the pattern already used in `TaskAddView`.
- **Impact**: The edit task dialog now shows all valid assignees (owner + members). The create task dialog (`TaskAddView`) is unaffected since it constructs its own list from the `ProjectDTO` directly.

## bugfix/project-tasks-navigation

### Design Changes
- **File**: `ProjectsView.java` — Tasks cell double-click handler
- **Problem**: Double-clicking the "Tasks" cell in the projects table opens `TaskAddView` (a create-task dialog) instead of navigating to the Tasks view filtered by that project. The task count cell semantically represents "show me these tasks", not "create a new task".
- **Solution**: Replace the `TaskAddView` creation with two calls: `setProjectFilter(projectName)` on `TaskViewModel`, then `refreshTasks()`. The `refreshTasks()` fires a `TASKS` notification, causing `MainFrame` to switch to `CARD_TASKS` and render the filtered task grid. Removed unused `TaskAddView` import.
- **Impact**: Double-clicking the Tasks cell now navigates to the Tasks view filtered by the clicked project. The "+ Add Task" button in the Tasks toolbar remains the entry point for task creation.

## bugfix/task-visibility-permissions

### Design Changes
- **Files**: No source code changes — verification tests only
- **Verified behaviors**:
  1. `TaskController.refreshTasks()` filters tasks by `listByAssignee(session.getLogged_in_user())` — users only see tasks assigned to them
  2. `ProjectsView.isCellEditable()` checks `owner.equals(currentUser)` for Name/Description columns — only project owners can inline-edit
  3. `ProjectsView` Description double-click handler also checks ownership before opening the editor
  4. When no session exists, `refreshTasks()` returns an empty list and project editing is blocked
- **Impact**: Added test coverage to confirm these permission checks work correctly. No behavioral changes.

## bugfix/project-add-member-error

### Design Changes
- **File**: `ProjectAddView.java` — Add Member button action listener
- **Problem**: Two bugs in the member addition flow:
  1. `currentMembers.add(trimmed)` runs BEFORE `projectController.addMember()`. If the service throws (e.g., user doesn't exist), the UI shows the member as added, but the server rejected it.
  2. No try-catch around `addMember()`. The `IllegalArgumentException` from `TrakProjectService` bubbles up as an uncaught EDT exception — stack trace in console, no user-facing error dialog.
- **Solution**: Swap the order — call `addMember()` first, then add to local list only on success. Wrap in try-catch with `JOptionPane.showMessageDialog` for error feedback.
- **Impact**: Users see a clear error dialog when trying to add a non-existent member. The local member list stays consistent with the server state.

## bug/stale-test-data-in-store

### Design Changes
- **Files**: `CMDTest.java`, `StepFunctions.java`, `ObserverPatternTest.java`, `ObservableViewModel.java`, `.gitignore`
- **Problem**: Four test files write to `.store` and `.cache` at the project root without redirecting or cleaning up. After `make test`, stale `Project.parquet` and `User.parquet` persist in `.store/`, causing ghost data (`testowner`, `Project_1`) to appear in the GUI.
- **Solution**:
  1. Redirect `CMDTest` and `StepFunctions` to `src/test/.store` with `@Before`/`@After` save-and-restore of `TTApp.storedir`
  2. Make `ObservableViewModel.CACHE_DIR` package-accessible and redirect `ObserverPatternTest` to `src/test/.cache`
  3. Add `src/test/.store/` and `src/test/.cache/` to `.gitignore`
- **Impact**: Tests no longer pollute `.store` or `.cache` at the project root. All test artifacts go to `src/test/` and are cleaned up after each run.

## feature/MainView-UXUI (Dark Cinematic Theme)

### Design Changes
- **Files**: `TrakTheme.java` (new), `GlassPanel.java` (new), `FormPanel.java` (new), `MainFrame.java`, `StatusPanel.java`, `CommandInputPanel.java`, `OutputPanel.java`, `FormDialogView.java`, `GUIMain.java`, all form views (LoginView, SignUpView, LogOutView, ProjectAddView, ProjectCreateView, SprintAddView, TaskAddView, TaskEditView)
- **Goal**: Replace default Swing look with a dark cinematic theme — deep charcoal background, warm gold accent, consistent typography and spacing.
- **Solution**: Created `TrakTheme.java` as a centralized theme with color palette, 8px spacing grid, typography scale (DISPLAY through CAPTION), and `applyDefaults()` for ~50 UIManager keys. `GlassPanel` provides rounded-corner gradient panels with optional drop shadow. `FormPanel` provides consistent two-column GridBagLayout for all form dialogs. `GUIMain` switched to cross-platform L&F for full color control. All form views refactored to return `FormPanel` from `buildPanel()`.
- **Impact**: Entire GUI renders in a consistent dark theme. All hardcoded colors replaced with TrakTheme constants. Form layout standardized across all 8 dialog views.

## feature/MainView-UXUI (Workspace Toggle)

### Design Changes
- **Files**: `MainFrame.java`, `TaskController.java`, `ProjectController.java`
- **Goal**: Allow users to toggle between personal workspace ("Mine" — filtered by logged-in user) and team workspace ("Team" — all data).
- **Solution**: Added `myWorkspaceBtn` and `teamWorkspaceBtn` to MainFrame nav bar with gold active / muted inactive toggle styling. `setTeamMode(boolean)` calls `refreshTasks(bool)` and `refreshProjects(bool)`. `TaskController.refreshTasks(true)` calls `listAll()`, `false` calls `listByAssignee()`. `ProjectController.refreshProjects(true)` calls `listAll()`, `false` calls `listByUser()`. State resets to Mine on logout.
- **Impact**: Users can now see all team tasks/projects or just their own. Existing behavior (Mine mode) is preserved as the default.

## feature/MainView-UXUI (TimeInput)

### Design Changes
- **Files**: `TimeInputPanel.java` (new), `TimeUtil.java`, `TaskAddView.java`, `TaskEditView.java`, `TaskController.java`, `TaskService.java`, `TrakTaskService.java`, `TaskHttpService.java`
- **Goal**: Replace free-text estimate field with structured duration spinners (days/hours/minutes).
- **Solution**: Created `TimeInputPanel` with three JSpinners (days 0–30, hours 0–24, minutes 0–59). `getDurationString()` returns `"Xd Yh Zm"` format. `setDuration()` parses existing estimates via `TimeUtil.parseDurationToComponents()`. `TaskEditView` now supports estimate editing (previously missing). `TaskService.updateById()` and `TaskController.updateTask()` gain `String estimate` parameter. CLI commands pass `null` for backward compatibility.
- **Impact**: Estimates are structured and validated. TaskEditView can modify estimates. Full stack supports estimate updates (service interface, server impl, HTTP client).

## feature/MainView-UXUI (Makefile + Build)

### Design Changes
- **Files**: `Makefile`, `gradle.properties` (new)
- **Goal**: Simplify Makefile targets and add per-component build targets.
- **Solution**: Added `build-gui`, `build-cli`, `build-server` targets for building individual jars. Added `reset` target (clean + rm .store .cache). Removed `server`, `gui`, `gui-test`, `gui-server`, `gui-test-server` shortcut targets — users now run jars directly. Added `gradle.properties` with daemon, parallel, and caching optimizations.
- **Impact**: Faster incremental builds. Cleaner Makefile. Users run `java -jar trak-gui --local --test` instead of `make gui-test`.
