# Diagram Changes

## bugfix/observer-exception-isolation

### Why It Wasn't Working

```
BEFORE (broken):
  notifyObservers(TASKS)
    |-- observer1.onChanged(TASKS) --> throws RuntimeException
    '-- observer2.onChanged(TASKS) --> NEVER REACHED (exception propagated)

AFTER (fixed):
  notifyObservers(TASKS)
    |-- observer1.onChanged(TASKS) --> throws RuntimeException --> caught, logged
    '-- observer2.onChanged(TASKS) --> CALLED SUCCESSFULLY
```

### Observer Notification Flow

```
ViewModel.setAll(data)
  |
  |-- updates internal state
  |
  '-- notifyObservers(type)
        |
        |-- for each listener in CopyOnWriteArrayList:
        |     try {
        |       listener.onViewModelChanged(type)
        |     } catch (Exception e) {
        |       stderr: "Observer notification failed: {message}"
        |     }
        |
        '-- all listeners guaranteed to run
```

## bugfix/logout-card-visibility

### Why It Wasn't Working

```
BEFORE (broken logout flow):
  onCommandExecuted(LOGOUT)
    |-- userViewModel.setSession(null) --> fires SESSION notification
    |     '-- MainFrame (invokeLater): clearViewModels() + show CARD_OUTPUT
    '-- clearViewModels()
          |-- taskVM.setAll([]) --> fires TASKS notification
          |     '-- MainFrame (invokeLater): show CARD_TASKS  (overrides OUTPUT)
          |-- projectVM.setAll([]) --> fires PROJECTS notification
          |     '-- MainFrame (invokeLater): show CARD_PROJECTS
          '-- sprintVM.setAll([]) --> fires SPRINTS notification
                '-- MainFrame (invokeLater): show CARD_SPRINTS  (user sees this!)

AFTER (fixed):
  onCommandExecuted(LOGOUT)
    '-- userViewModel.setSession(null) --> fires SESSION notification
          '-- MainFrame (invokeLater):
                |-- updateStatus() --> shows "Not logged in", hides logout btn
                |-- clearViewModels() --> fires TASKS/PROJECTS/SPRINTS
                |     '-- MainFrame handlers: session==null --> return (no-op)
                |-- setNavButtonsEnabled(false) --> disables nav buttons
                '-- show CARD_OUTPUT  (stays on output panel)
```

### Cache Lifecycle

```
.cache/                              .store/
  task_viewmodel.ser                   session.json
  project_viewmodel.ser                *.parquet / *.json / MongoDB
  sprint_viewmodel.ser
  user_viewmodel.ser

WRITE:  GUIController.save() --> ViewModel.save() --> ObjectOutputStream --> .cache/
LOAD:   ViewModel.load() --> ObjectInputStream --> loadFrom()
CLEAR:  clearViewModels() --> setAll([]) + clearCache() --> delete .cache/*.ser
```

## bugfix/task-assignee-selection

### Why It Wasn't Working

```
BEFORE (broken):
  TasksView.render()
    '-- getProjectMembers("MyProject")
          '-- returns project.memberUsernames()  -->  ["alice", "bob"]
                                                      (owner "charlie" missing!)
          '-- TaskCardPanel(task, controller, ["alice", "bob"])
                '-- Edit dialog assignee dropdown: [alice, bob]
                    (cannot reassign to owner charlie)

AFTER (fixed):
  TasksView.render()
    '-- getProjectMembers("MyProject")
          '-- builds: [owner] + [members - owner]  -->  ["charlie", "alice", "bob"]
          '-- TaskCardPanel(task, controller, ["charlie", "alice", "bob"])
                '-- Edit dialog assignee dropdown: [charlie, alice, bob]
```

### Data Flow: Assignee Population

```
ProjectDTO
  |-- ownerUsername: "charlie"
  '-- memberUsernames: ["alice", "bob"]

getProjectMembers() builds:
  result = []
  result.add(ownerUsername)       -->  ["charlie"]
  for m in memberUsernames:
    if m != ownerUsername:
      result.add(m)              -->  ["charlie", "alice", "bob"]
  return result
```

## bugfix/project-tasks-navigation

### Why It Wasn't Working

```
BEFORE (wrong behavior):
  ProjectsView: double-click "Tasks" cell (row for "MyProject")
    '-- opens TaskAddView dialog (create new task)
         '-- pre-selects "MyProject" in project dropdown
         User wanted to SEE tasks, not CREATE one

AFTER (fixed):
  ProjectsView: double-click "Tasks" cell (row for "MyProject")
    |-- TaskViewModel.setProjectFilter("MyProject")
    '-- TaskController.refreshTasks()
          '-- fires TASKS notification
                '-- MainFrame switches to CARD_TASKS
                      '-- TasksView.render() shows tasks filtered by "MyProject"
```

### Navigation Flow

```
ProjectsView (table)
  |-- double-click "Members" cell --> ProjectAddView (manage members)
  |-- double-click "Description" cell --> showSummaryEditor (if owner)
  '-- double-click "Tasks" cell --> navigate to TasksView with filter
        |
        TaskViewModel.setProjectFilter("ProjectName")
        TaskController.refreshTasks()
          '-- setAll(tasks) --> notifyObservers(TASKS)
                '-- MainFrame.onViewModelChanged(TASKS)
                      |-- cardLayout.show(CARD_TASKS)
                      '-- tasksView.render()
                            '-- toolbar shows "Project: [ProjectName]"
                            '-- cards filtered to ProjectName
```

## bugfix/task-visibility-permissions

### Task Visibility Flow

```
TaskController.refreshTasks()
  |
  |-- session != null && session.getLogged_in_user() != null?
  |     YES --> tasks = taskService.listByAssignee(username)
  |     NO  --> tasks = List.of()  (empty)
  |
  '-- taskViewModel.setAll(tasks)
        '-- only assigned tasks shown in TasksView
```

### Project Edit Permission Flow

```
ProjectsView.isCellEditable(row, col)
  |
  |-- column is "Name" or "Description"?
  |     NO  --> return false (not editable)
  |     YES --> continue
  |
  |-- owner = projects.get(row).ownerUsername()
  |-- currentUser = session.getLogged_in_user()
  |
  '-- owner.equals(currentUser)?
        YES --> return true (editable)
        NO  --> return false (read-only)
```

## bugfix/project-add-member-error

### Why It Wasn't Working

```
BEFORE (broken):
  User clicks "+ Add Member" --> types "manuel"
    |-- currentMembers.add("manuel")        <-- added to UI list FIRST
    '-- projectController.addMember("manuel")
          '-- TrakProjectService.addMember()
                '-- DAOFactory.userDAO().loadByKey("manuel")
                      '-- returns null (user doesn't exist)
                '-- throws IllegalArgumentException("User \"manuel\" not found.")
                      '-- uncaught on EDT --> stack trace in console
                      '-- UI still shows "manuel" in member list (stale)

AFTER (fixed):
  User clicks "+ Add Member" --> types "manuel"
    '-- try {
          projectController.addMember("manuel")
            '-- throws IllegalArgumentException
        } catch (Exception ex) {
          JOptionPane.showMessageDialog("User \"manuel\" not found.")
        }
        // currentMembers.add("manuel") NEVER runs
        // UI stays consistent with server state
```

### Add Member Flow

```
ProjectAddView: "+ Add Member" button
  |
  |-- JOptionPane.showInputDialog("Enter username")
  |     '-- user types "manuel"
  |
  |-- currentMembers.contains("manuel")?
  |     YES --> skip (no duplicate)
  |     NO  --> continue
  |
  '-- try:
        projectController.addMember(projectName, "manuel")
          '-- TrakProjectService.addMember()
                |-- loadByKey("manuel") --> User found? 
                |     YES --> add to project members, save
                |     NO  --> throw IllegalArgumentException
                |
        currentMembers.add("manuel")  // only on success
        refresh UI
      catch:
        show error dialog to user
```
