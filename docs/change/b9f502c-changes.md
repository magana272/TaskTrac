# Add dark cinematic theme, workspace toggle, time input spinners, and green CTA styling

- **Commit**: `b9f502c`
- **Branch**: `feature/MainView-UXUI`

## What Changed

### Feature 1: Dark Cinematic Theme

**New files**: `TrakTheme.java`, `GlassPanel.java`, `FormPanel.java`
**Modified**: `MainFrame.java`, `StatusPanel.java`, `CommandInputPanel.java`, `OutputPanel.java`, `FormDialogView.java`, `GUIMain.java`, `LoginView.java`, `SignUpView.java`, `LogOutView.java`, `ProjectAddView.java`, `ProjectCreateView.java`, `SprintAddView.java`, `TaskAddView.java`, `TaskEditView.java`

- `TrakTheme` centralizes the entire color palette (deep charcoal BG, warm gold accent), 8px spacing grid, typography scale (DISPLAY→CAPTION), and ~50 UIManager defaults. Provides button/card/table/combo styling methods.
- `GlassPanel` renders rounded-corner gradient panels with optional drop shadow and glass reflection.
- `FormPanel` provides a two-column GridBagLayout form builder used by all 8 form dialogs.
- `GUIMain` switched to cross-platform L&F for full color control; calls `TrakTheme.applyDefaults()`.
- `FormDialogView.buildPanel()` return type changed from `JPanel` to `FormPanel`.
- All form views refactored to use `FormPanel.addField()`.

### Feature 2: Workspace Toggle (Mine/Team)

**Modified**: `MainFrame.java`, `TaskController.java`, `ProjectController.java`

- MainFrame adds "⌂ Mine" and "✳ Team" toggle buttons in the nav bar with gold active / muted inactive styling.
- `setTeamMode(boolean)` calls `refreshTasks(bool)` and `refreshProjects(bool)`.
- `TaskController.refreshTasks(true)` calls `listAll()`; `false` calls `listByAssignee()`.
- `ProjectController.refreshProjects(true)` calls `listAll()`; `false` calls `listByUser()`.
- Resets to Mine on logout.

### Feature 3: TimeInput (Duration Spinners)

**New file**: `TimeInputPanel.java`
**Modified**: `TimeUtil.java`, `TaskAddView.java`, `TaskEditView.java`, `TaskController.java`, `TaskService.java`, `TrakTaskService.java`, `TaskHttpService.java`, `TaskUpdateCMD.java`, `CompleteCMD.java`, `EndTaskCMD.java`, `StartTaskCMD.java`, `GUIController.java`

- `TimeInputPanel` provides days (0–30), hours (0–24), minutes (0–59) JSpinners with `getDurationString()`, `setDuration()`, `isZero()`.
- `TimeUtil.parseDurationToComponents()` and `parseDurationToMs()` parse `"Xd Yh Zm"` format.
- `TaskAddView` replaces free-text field with `TimeInputPanel`.
- `TaskEditView` now supports estimate editing (previously missing).
- `TaskService.updateById()` gains `String newEstimate` parameter. Full stack updated.
- CLI commands pass `null` for estimate (backward compatible).

### Feature 4: Green CTA + Task Card Redesign

**Modified**: `TaskCardPanel.java`, `TasksView.java`

- Add Task buttons styled via `TrakTheme.styleButtonPrimary()` (green CTA).
- Task cards redesigned with TrakTheme colors, gradient backgrounds, gold glow hover, status-colored combo.

### Makefile Changes

**Modified**: `Makefile`
**New file**: `gradle.properties`

- Added: `build-gui`, `build-cli`, `build-server` (per-component build), `reset` (clean + rm .store .cache).
- Removed: `server`, `gui`, `gui-test`, `gui-server`, `gui-test-server` shortcut targets.
- `gradle.properties` enables daemon, parallel builds, and caching.

## Why

The GUI was functional but visually raw — default Swing look, no theme consistency, no workspace scoping, free-text estimate input. These features transform it into a polished desktop application with consistent styling, user-scoped views, and structured input.

## Risks

- Cross-platform L&F may render slightly differently on Linux/Windows vs macOS.
- Existing tasks with free-text estimates (e.g., "about 2 hours") won't parse into the spinners — defaults to 0d 0h 0m.
- Custom `paintComponent` in TaskCardPanel and GlassPanel adds rendering overhead for large task lists.

## Known Limitations

- No runtime theme switching — TrakTheme is static constants, not a properties file.
- Workspace toggle does not affect SprintView (sprints are not user-scoped).
- Max estimate upper bound is 30 days.
