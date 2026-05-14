# Fix add member error handling in ProjectAddView

- **Found at**: 789e75b
- **Fixed at**: 4f2d261
- **Branch**: `bugfix/project-add-member-error`

## Problem

Adding a non-existent user as project member throws uncaught `IllegalArgumentException` on the EDT. Stack trace in console, no user-facing error. Member is incorrectly added to the local UI list.

## Root Cause

`ProjectAddView` add-member handler calls `currentMembers.add(trimmed)` before `projectController.addMember()`. On service failure, the UI shows a phantom member. No try-catch wraps the call.

## Fix

**File**: `ProjectAddView.java`

Swap order: call `addMember()` first, add to local list only on success. Wrap in try-catch with `JOptionPane.showMessageDialog`.

## Diagram

```
BEFORE:
  click "+ Add Member" --> "manuel"
    |-- currentMembers.add("manuel")   <-- UI updated FIRST
    '-- addMember("manuel") --> throws --> uncaught EDT exception
        UI shows "manuel" but server rejected it

AFTER:
  click "+ Add Member" --> "manuel"
    '-- try: addMember("manuel") --> throws
        catch: showMessageDialog("User \"manuel\" not found.")
        currentMembers NOT modified
```

## Tests

- **New**: `ProjectAddMemberTest.java` (4 tests)
