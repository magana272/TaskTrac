# Add verification tests for task visibility and project permissions

- **Found at**: 789e75b
- **Fixed at**: 9cac356
- **Branch**: `bugfix/task-visibility-permissions`

## Problem

Need to confirm that users only see assigned tasks and non-owners cannot edit project name/description.

## Analysis

Both behaviors already work correctly:
- `TaskController.refreshTasks()` filters by `listByAssignee(session.getLogged_in_user())`
- `ProjectsView.isCellEditable()` checks `owner.equals(currentUser)`

No source code changes needed. Verification tests only.

## Diagram

```
Task Visibility:
  refreshTasks() --> session? --> listByAssignee(user) / empty

Project Permissions:
  isCellEditable(row, col)
    col is Name/Description? --> owner.equals(currentUser)? --> true/false
```

## Tests

- **New**: `TaskVisibilityTest.java` (5 tests)
