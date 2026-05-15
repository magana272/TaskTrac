# DESIGN: UI Polish, Responsive Cards, and Focus Timer

---

## 1. Goal

Transform the Trak GUI from a functional prototype into a polished solo-dev productivity tool. Three changes:

1. **UI Consistency** — Standardize all button sizes, fonts, and spacing so the interface feels cohesive
2. **Responsive Task Cards** — Cards resize to fill the window; when space is too tight, switch to a compact row layout
3. **Focus Timer Bar** — When a task is IN PROGRESS in a sprint, show a live countdown bar (green→yellow→red) on the
   card. On completion, prompt the user for a reflection note.

## 2. Success Criteria

- [x] All "+" buttons (Add Project, Add Task, Add Sprint) are identical size and style
- [x] All combo boxes are the same height
- [x] Cards expand to fill available width with no horizontal scroll
- [ ] Below ~400px per card, layout switches to compact single-row list
- [x] IN PROGRESS tasks with an estimate show a live timer bar (updates every second)
- [x] Timer bar transitions: green (0-70%), amber (70-100%), red (>100%)
- [x] Completing a task opens a "What did you accomplish?" dialog
- [x] Timer bar only appears on tasks that belong to a sprint
- [] No new external dependencies

## 3. Constraints

- Must work with existing `EntityDAO<T>` interface — no schema changes
- Timer display is client-side only — no server polling every second
- `TaskDTO.timeSpentMs` is a server-side snapshot; client interpolates locally
- Must preserve all existing functionality (edit, delete, status change, sort, filter)
- No new dependencies

## 4. UX Expectations

### Card Layout — Wide Window (>800px)

```
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ Setup CI/CD      │  │ Login Screen     │  │ Write Tests      │
│ ● INPROGRESS  [X]│  │ ● READY       [X]│  │ ✓ COMPLETE    [X]│
│ MobileApp        │  │ MobileApp        │  │ MobileApp        │
│ Configure pipe...│  │ Figma mockups    │  │ Unit + integ     │
│ Due: May 21      │  │ Due: May 22      │  │ Completed        │
│ Est: 3h   #1234  │  │ Est: 8h   #1235  │  │ 2h 15m    #1236  │
│ ████████░░ 2h/3h │  │                  │  │                  │
│ [GREEN────→]     │  │                  │  │                  │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

### Card Layout — Narrow Window (<400px per card)

```
● INPROG  Setup CI/CD          MobileApp   3h  May 21  ████ 2h/3h
● READY   Login Screen         MobileApp   8h  May 22
✓ DONE    Write Tests          MobileApp   2h  Completed
```

### Timer Bar States

```
0%──────────────────50%──────────────────100%──────────→ overtime
[         GREEN          ][    AMBER    ][ RED →→→→→→→→→→ ]
```

### Completion Dialog

```
┌─────────────────────────────────────┐
│  Task Complete: "Setup CI/CD"       │
│                                     │
│  What did you accomplish?           │
│  ┌─────────────────────────────┐   │
│  │ Configured GitHub Actions   │   │
│  │ pipeline for staging deploy │   │
│  └─────────────────────────────┘   │
│                                     │
│  Time spent: 2h 47m (est: 3h)     │
│                                     │
│            [Save]  [Skip]           │
└─────────────────────────────────────┘
```

---

## 5. Architecture Decisions

### 5.1 Client-Side Timer Interpolation

**Problem:** `TaskDTO.timeSpentMs` is a snapshot from the server. The client can't poll every second.

**Solution:** Track `lastRefreshTimestamp` per task view render. For INPROGRESS tasks:

```
displayTime = dto.timeSpentMs + (System.currentTimeMillis() - lastRefreshTimestamp)
```

This gives a smoothly incrementing display without any server calls. On the next data refresh (status change, manual
refresh), the server provides the authoritative accumulated time, and the client resets its local offset.

**Why this works:**

- At refresh time t0: server says `timeSpentMs = X` (includes running time up to t0)
- At t0+1s: client shows `X + 1000ms` — correct
- At next refresh t1: server says `timeSpentMs = X + (t1-t0)` — client resets
- No drift, no double-counting

**Alternative considered:** Expose `time_started` in TaskDTO and have client compute elapsed. Rejected because it leaks
server-side internal state and requires DTO/schema change.

### 5.2 Compact Layout Switch

**Problem:** Fixed-size cards scroll horizontally or leave gaps when window is narrow.

**Solution:** `TasksView.layoutCards()` already calculates `cols = max(1, (width+gap) / (minWidth+gap))`. Add a
threshold: if `cols == 1` AND `containerWidth < 400`, switch to compact row mode.

Compact mode renders each task as a single `JPanel` row with `BoxLayout.X_AXIS`:

```
[StatusDot 12px] [Title 40%] [Project 20%] [Estimate 10%] [Deadline 15%] [TimerBar 15%]
```

Card mode and compact mode share the same data source (`getFiltered()`). The switch is purely visual.

### 5.3 Timer Bar as Custom Paint

**Problem:** Adding a Swing component for the timer bar adds complexity.

**Solution:** Paint the timer bar directly in `TaskCardPanel.paintComponent()`, below the existing card content. This
is:

- Zero-allocation (no new components)
- Consistent with existing gradient/glow painting
- Updates via `repaint()` called from a shared `javax.swing.Timer`

The bar is 6px tall, drawn at the bottom of the card's rounded rectangle. Color determined by `elapsed / estimate`
ratio.

### 5.4 Shared Swing Timer

**Problem:** Each card creating its own timer is wasteful.

**Solution:** `TasksView` owns a single `javax.swing.Timer(1000, ...)` that calls `repaint()` on all visible INPROGRESS
cards. Timer starts when TasksView is shown, stops when hidden. One timer, many cards.

### 5.5 Completion Prompt

**Problem:** Need to capture reflection when task completes without blocking the status change.

**Solution:** When status combo changes to COMPLETE, show a `JOptionPane` with a `JTextArea`. If user clicks Save,
append the note to the task's summary via `taskController.updateTask(id, null, null, null, newSummary, null)`. If Skip,
complete without note.

The completion prompt only fires when the user manually changes status to COMPLETE via the combo box — not on data
refresh or external changes.

---

## 6. Data Flow

### Timer Bar

```
TasksView.render()
  → stores lastRefreshTimestamp = System.currentTimeMillis()
  → creates TaskCardPanel for each task

javax.swing.Timer (every 1s)
  → for each visible INPROGRESS card:
      elapsed = dto.timeSpentMs + (now - lastRefreshTimestamp)
      ratio = elapsed / TimeUtil.parseDurationToMs(dto.estimate)
      card.setTimerRatio(ratio, elapsed)  // stores values
      card.repaint()  // triggers paintComponent

TaskCardPanel.paintComponent()
  → draws existing card (gradient, border, glow)
  → if task.status == INPROGRESS && estimate != null:
      draws timer bar at bottom (6px tall)
      color = ratio < 0.7 ? GREEN : ratio < 1.0 ? AMBER : RED
      fills rounded rect proportional to min(ratio, 1.0)
      if ratio > 1.0: full bar in RED
```

### Completion Prompt

```
StatusCombo.actionListener fires "COMPLETE"
  → show JOptionPane with JTextArea
  → if SAVE:
      note = textArea.getText()
      existingSummary = task.summary()
      newSummary = existingSummary + "\n\n--- Completed ---\n" + note
      taskController.updateTask(id, null, "COMPLETE", null, newSummary, null)
  → if SKIP:
      taskController.completeTask(id)
```

### Responsive Layout

```
Window resizes → ComponentListener fires
  → containerWidth = panel.getWidth()
  → cols = max(1, (containerWidth + gap) / (minCardWidth + gap))
  → if cols == 1 && containerWidth < 400:
      renderCompactMode(tasks)  // single-row per task
  → else:
      renderCardMode(tasks, cols)  // existing grid
```

---

## 7. State Management

| State                     | Where                      | Lifecycle                                                      |
|---------------------------|----------------------------|----------------------------------------------------------------|
| `lastRefreshTimestamp`    | `TasksView` field          | Set on each `render()`, reset on re-render                     |
| `timerRatio`, `elapsedMs` | `TaskCardPanel` fields     | Set by Timer callback, read by paintComponent                  |
| `javax.swing.Timer`       | `TasksView` field          | Started on first render, stopped on 0 visible INPROGRESS tasks |
| Compact vs card mode      | `TasksView` local variable | Recalculated on each layout pass                               |

No new ViewModel state. No server changes. No DTO changes.

---

## 8. Component Responsibilities

| Component        | Responsibility                                                         |
|------------------|------------------------------------------------------------------------|
| `TrakTheme`      | Button height constant, timer bar colors                               |
| `TaskCardPanel`  | Paints timer bar, handles completion prompt, responsive sizing         |
| `TasksView`      | Owns swing Timer, calculates elapsed offsets, responsive layout switch |
| `TaskController` | Unchanged — existing updateTask/completeTask handle status + summary   |
| `TimeUtil`       | Unchanged — existing parseDurationToMs for estimate parsing            |

---

## 9. Testing Strategy

### Cucumber Scenarios (new feature file: `timer.feature`)

```gherkin
Feature: Focus Timer

  Scenario: Timer bar appears on INPROGRESS task with estimate
    Given a task with estimate "2h" in sprint "Sprint1"
    When the task status changes to INPROGRESS
    Then the task card shows a timer bar

  Scenario: Timer bar not shown on READY task
    Given a task with estimate "2h" in sprint "Sprint1"
    And the task status is READY
    Then the task card does not show a timer bar

  Scenario: Completion prompt on status change
    Given a task with status INPROGRESS
    When the user changes status to COMPLETE
    Then a completion dialog appears
    And the user can enter accomplishment text
```

### Unit Tests

- `TimeUtil.parseDurationToMs("2h")` → 7200000
- `TimeUtil.parseDurationToMs("1d 4h 30m")` → 102600000
- Timer ratio calculation: elapsed=3600000, estimate=7200000 → ratio=0.5
- Color thresholds: ratio=0.5→GREEN, ratio=0.8→AMBER, ratio=1.2→RED
- Compact mode threshold: width=350→compact, width=500→cards

### Edge Cases

- Task with no estimate → no timer bar (just shows card normally)
- Task not in any sprint → no timer bar
- Estimate of "0d 0h 0m" → treated as no estimate
- Timer ratio > 2.0 → cap red bar at full width, keep counting time text
- Window resize during timer → layout recalculates, timer continues
- Multiple INPROGRESS tasks → each has independent timer bar
- Task completes via CLI while GUI is open → next refresh removes timer bar

### Failure Cases

- `parseDurationToMs` returns 0 for invalid estimate → no timer bar
- Timer fires but card has been removed (task deleted) → card not in list, skip
- Completion dialog cancelled → no summary change, status still changes to COMPLETE

---

## 10. Tradeoffs

| Decision                        | Alternative               | Rationale                                                |
|---------------------------------|---------------------------|----------------------------------------------------------|
| Client-side interpolation       | Server push / WebSocket   | Overkill for localhost; interpolation is accurate enough |
| Paint timer in paintComponent   | Separate JPanel component | Less overhead, consistent with existing card painting    |
| Single shared javax.swing.Timer | Per-card timer            | One timer manages all cards, scales to any count         |
| Compact row at <400px           | Always cards              | Dense information display when space is tight            |
| Append note to summary          | New "reflection" field    | Avoids DTO/schema change; summary is the right place     |
| Timer only for sprint tasks     | All INPROGRESS tasks      | Sprint context gives the "focus session" meaning         |

---

## 11. Risks and Known Limitations

- **Timer accuracy:** Client interpolation drifts ~1s between refreshes. Acceptable for a focus timer.
- **Compact mode is new code:** No existing compact row renderer. Must be built from scratch.
- **Completion prompt blocking:** JOptionPane blocks EDT. Keep it simple (no async).
- **timeSpentMs not set by GUI:** Currently only CLI sets `time_started` via StartTaskCMD. The GUI status change to
  INPROGRESS should also start the timer server-side. **This requires a server-side fix:**
  `TrakTaskService.updateById()` must set `time_started = System.currentTimeMillis()` when status changes to INPROGRESS,
  and accumulate time when status changes FROM INPROGRESS.

---

## 12. Files to Modify

| File                                           | Changes                                                                                                           |
|------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| `gui/view/task/TaskCardPanel.java`             | Remove fixed CARD_WIDTH/CARD_HEIGHT. Add timer bar paint. Add completion prompt. Add timerRatio/elapsedMs fields. |
| `gui/view/task/TasksView.java`                 | Responsive layout with compact fallback. javax.swing.Timer for 1s ticks. Store lastRefreshTimestamp.              |
| `gui/view/sprint/SprintProgressPanel.java`     | Change addSprintBtn from styleButtonNav to styleButtonPrimary                                                     |
| `gui/view/TrakTheme.java`                      | Add TIMER_GREEN, TIMER_AMBER, TIMER_RED constants (can reuse existing STATUS colors). Add BUTTON_HEIGHT constant. |
| `app/server/service/task/TrakTaskService.java` | Set time_started on INPROGRESS, accumulate time_spent_ms on status change FROM INPROGRESS                         |

## 13. Files to Create

| File                                | Purpose                                    |
|-------------------------------------|--------------------------------------------|
| `gui/view/task/CompactTaskRow.java` | Single-row task display for narrow windows |

## 14. Implementation Order

1. **Server fix:** TrakTaskService time tracking on status change (prerequisite for timer)
2. **UI polish:** Button consistency, font standardization
3. **Responsive cards:** Remove fixed dimensions, compact fallback
4. **Timer bar:** Paint in TaskCardPanel, swing Timer in TasksView
5. **Completion prompt:** Dialog on COMPLETE status change

## 15. Verification

- `./gradlew compileJava` — success
- `./gradlew test --rerun` — all pass
- Manual: resize window wide→narrow→wide, verify card↔compact transition
- Manual: all buttons identical size
- Manual: create task with 1-minute estimate, set to INPROGRESS → watch bar go green→amber→red
- Manual: complete task → prompted for note → note appears in task summary
