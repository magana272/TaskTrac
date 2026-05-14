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
