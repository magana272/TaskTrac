# Style Add Task button with green primary-action color

- **Branch**: `feature/TaskView-green-add`

## What Changed

**File**: `TasksView.java`

1. Added `ADD_BTN_COLOR` constant (`#34C759` Apple green)
2. Added `styleAddButton(JButton)` helper method — sets green background, white text, opaque, no border/focus paint, hand cursor
3. Applied `styleAddButton()` to both Add Task button instances:
   - Empty-state placeholder button (`+ Add a Task`)
   - Toolbar button (`+ Add Task`)

## Why

The Add Task button was visually identical to all other buttons. As the primary CTA, it needs to stand out to guide user action.

## Risks

- None — isolated cosmetic change, no logic modified

## Known Limitations

- Color is hardcoded; will be replaced by theme system in Feature 3 (MainView-UXUI)
