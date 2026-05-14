# Fix Tasks cell click to navigate to filtered task view

- **Found at**: 789e75b
- **Fixed at**: f7154bb
- **Branch**: `bugfix/project-tasks-navigation`

## Problem

Double-clicking the "Tasks" cell in the projects table opens `TaskAddView` (create dialog) instead of navigating to the Tasks view filtered by that project.

## Root Cause

`ProjectsView` handles the Tasks cell double-click by creating a `TaskAddView` dialog. The task count cell semantically means "show me these tasks", not "create a new task".

## Fix

**File**: `ProjectsView.java`

Replace `TaskAddView` creation with `setProjectFilter(projectName)` + `refreshTasks()`. Removed unused `TaskAddView` import.

## Diagram

```
BEFORE:
  double-click "Tasks" cell --> opens TaskAddView dialog (wrong)

AFTER:
  double-click "Tasks" cell
    |-- setProjectFilter("MyProject")
    '-- refreshTasks() --> TASKS notification
          --> MainFrame --> CARD_TASKS --> filtered by "MyProject"
```

## Tests

- **New**: `ProjectTasksNavigationTest.java` (4 tests)
