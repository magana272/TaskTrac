# Fix logout to hide all cards and prevent editing

- **Found at**: 789e75b
- **Fixed at**: 876d8e0
- **Branch**: `bugfix/logout-card-visibility`

## Problem

After logout, user can still see and edit task/project/sprint cards. The UI does not return to the guest output panel. Stale cache files persist.

## Root Cause

`GUIController.onCommandExecuted(LOGOUT)` calls `setSession(null)` then `clearViewModels()`. The latter fires TASKS/PROJECTS/SPRINTS notifications that cause `MainFrame.onViewModelChanged` to switch away from `CARD_OUTPUT` via `SwingUtilities.invokeLater()`. The SPRINTS handler runs last, so the user ends up on the sprints view.

## Fix

**Files**: `MainFrame.java`, `GUIController.java`, `ObservableViewModel.java`

1. Guard TASKS/PROJECTS/SPRINTS handlers — return early if session is null
2. Disable nav buttons on logout, enable on login
3. Remove redundant `clearViewModels()` from LOGOUT case
4. Add `clearCache()` to delete `.cache/*.ser` on logout

## Diagram

```
BEFORE:
  LOGOUT --> setSession(null) --> SESSION --> show CARD_OUTPUT
          --> clearViewModels()
                TASKS --> show CARD_TASKS (overrides!)
                SPRINTS --> show CARD_SPRINTS (user sees this)

AFTER:
  LOGOUT --> setSession(null) --> SESSION handler:
        clearViewModels() --> handlers: session==null --> no-op
        setNavButtonsEnabled(false)
        show CARD_OUTPUT (stays)

CACHE: clearViewModels() --> setAll([]) + clearCache() --> delete .cache/*.ser
```

## Tests

- **New**: `LogoutViewClearTest.java` (4 tests)
- **New**: Cucumber scenario "Logout clears all view data" in `auth.feature`
