# Fix observer exception isolation in notifyObservers

- **Found at**: 789e75b
- **Fixed at**: 9633e17
- **Branch**: `bugfix/observer-exception-isolation`

## Problem

One throwing observer prevents all subsequent observers from receiving notifications.

## Root Cause

`ObservableViewModel.notifyObservers()` iterates listeners without try-catch. An exception in any listener propagates out, killing the loop.

## Fix

**File**: `ObservableViewModel.java`

Wrap each `listener.onViewModelChanged(type)` call in try-catch. Exceptions are logged to stderr but do not interrupt remaining observers.

## Diagram

```
BEFORE:
  notifyObservers(TASKS)
    |-- observer1.onChanged(TASKS) --> throws RuntimeException
    '-- observer2.onChanged(TASKS) --> NEVER REACHED

AFTER:
  notifyObservers(TASKS)
    |-- observer1.onChanged(TASKS) --> throws --> caught, logged
    '-- observer2.onChanged(TASKS) --> CALLED SUCCESSFULLY
```

## Tests

- **Updated**: `ObserverPatternTest.testObserverExceptionDoesNotBreakOtherObservers`
