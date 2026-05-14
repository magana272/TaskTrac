# DESIGN: feature/TimeInput

## Goal
Create a structured duration input component for task estimates, replacing the free-text field.

## Success Criteria
- Numeric input fields: days [0–30], hours [0–24], minutes [0–59]
- Total duration cannot equal 0d 0h 0m (validated on submit)
- Invalid ranges rejected immediately via spinner bounds
- Clean inline layout, keyboard-friendly
- Used in both TaskAddView and TaskEditView
- TaskEditView now supports editing estimate (currently missing)

## Architecture Decisions

### New file: `TimeInputPanel.java`
- Package: `task.trak.app.client.gui.view.task`
- Extends `JPanel` — reusable inline component
- Three `JSpinner` fields with `SpinnerNumberModel` for days/hours/minutes
- `getDurationString()` → returns `"Xd Yh Zm"` format string
- `setDuration(String)` → parses existing estimate string into spinner values
- `isZero()` → validation check

### Modified: `TimeUtil.java`
- Add `parseDurationToComponents(String)` → returns `int[] {days, hours, minutes}`
- Parses format like `"2d 5h 30m"`, `"5h"`, `"30m"`, etc.

### Modified: `TaskAddView.java`
- Replace `JTextField estimateField` with `TimeInputPanel estimatePanel`
- Validation: reject if `estimatePanel.isZero()`
- `onConfirm()`: pass `estimatePanel.getDurationString()` as estimate

### Modified: `TaskEditView.java`
- Add `TimeInputPanel estimatePanel` pre-populated from `task.estimate()`
- Add estimate to change detection in `onConfirm()`
- Pass estimate through to controller

### Modified: `TaskController.java`
- `updateTask()` gains `String estimate` parameter

### Modified: `TaskService.java` (interface)
- `updateById()` gains `String newEstimate` parameter

### Modified: `TrakTaskService.java` (server impl)
- Handle `newEstimate` in `updateById()`

### Modified: `TaskHttpService.java` (HTTP impl)
- Send `estimate` in PUT body

## UI/UX Approach
```
Estimate: [ 0 ▲▼] days  [ 0 ▲▼] hours  [ 0 ▲▼] minutes
```
- Horizontal FlowLayout
- Spinners with up/down arrows
- Labels between spinners for clarity
- Tab key moves between spinners

## Data Flow
```
TimeInputPanel.getDurationString()  →  "2d 5h 30m"
    ↓
TaskController.addTask/updateTask(... estimate)
    ↓
TaskService.create/updateById(... estimate)
    ↓
Task.setEstimate("2d 5h 30m")  →  persisted as String
```

## State Management
- No ViewModel changes — estimate is already on TaskDTO

## Testing Strategy
- Unit test `TimeUtil.parseDurationToComponents()` for various formats
- Unit test `TimeInputPanel.isZero()` validation
- Cucumber scenario: add task with estimate, verify estimate persists

## Tradeoffs
- Storing as formatted String vs millis: keeping String for backward compatibility with existing data. Format is human-readable and parseable.
- Max 30 days: reasonable upper bound for task estimates; can be adjusted later

## Risks
- Existing tasks with free-text estimates (e.g., "about 2 hours") won't parse cleanly into the spinners — `setDuration()` will default to 0/0/0 for unparseable strings
