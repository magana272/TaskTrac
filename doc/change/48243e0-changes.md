# Fix assignee dropdown to include project owner

- **Found at**: 789e75b
- **Fixed at**: 48243e0
- **Branch**: `bugfix/task-assignee-selection`

## Problem

Task edit dialog assignee dropdown does not include the project owner. Users cannot reassign tasks to the owner.

## Root Cause

`ProjectController.getProjectMembers()` returns `project.memberUsernames()` which excludes `ownerUsername`. `TaskCardPanel` uses this list directly for its edit dialog dropdown.

## Fix

**File**: `ProjectController.java`

Rebuild list: add owner first, then non-owner members (deduplicating).

## Diagram

```
BEFORE:
  getProjectMembers("Proj") --> memberUsernames: ["alice", "bob"]
                                (owner "charlie" missing)

AFTER:
  getProjectMembers("Proj") --> [owner] + [members - owner]
                                ["charlie", "alice", "bob"]
```

## Tests

- **New**: `ProjectMembersTest.java` (5 tests)
- **New**: Cucumber scenario "Task assignee can be changed to project member" in `task.feature`
