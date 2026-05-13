Feature: Workspace Commands

  Scenario: List my projects
    Given the workspace user "workuser" is logged in
    And a project "WorkProject" exists with owner "workuser"
    When the user runs the command "tasktracker projects"
    Then the output contains "WorkProject"

  Scenario: List my projects when none exist
    Given the workspace user "emptyuser" is logged in
    When the user runs the command "tasktracker projects"
    Then the output contains "No projects found"

  Scenario: List my tasks
    Given the workspace user "tasklistuser" is logged in
    And a project named "TaskListProj" exists
    And a task assigned to "tasklistuser" exists in project "TaskListProj" with title "My Task"
    When the user runs the command "tasktracker tasks"
    Then the output contains "My Task"

  Scenario: Show current task when none in progress
    Given the workspace user "nocuruser" is logged in
    When the user runs the command "tasktracker cur"
    Then the output contains "No task in progress"

  Scenario: Start a task
    Given the workspace user "startuser" is logged in
    And a project named "StartProj" exists
    And a task assigned to "startuser" exists in project "StartProj" with title "Start Me"
    When the user starts the created task
    Then the output contains "Started"
    And the created task has status "INPROGRESS"

  Scenario: Show current task after starting
    Given the workspace user "curuser" is logged in
    And a project named "CurProj" exists
    And a task assigned to "curuser" exists in project "CurProj" with title "Current Task"
    And the user starts the created task
    When the user runs the command "tasktracker cur"
    Then the output contains "Current Task"
    And the output contains "Time spent"

  Scenario: End a task
    Given the workspace user "enduser" is logged in
    And a project named "EndProj" exists
    And a task assigned to "enduser" exists in project "EndProj" with title "End Me"
    And the user starts the created task
    When the user runs the command "tasktracker end"
    Then the output contains "Stopped"
    And the created task has status "READY"

  Scenario: Detail shows project info
    Given the workspace user "detailuser" is logged in
    And a project "DetailProject" exists with owner "detailuser"
    When the user runs the command "tasktracker detail DetailProject"
    Then the output contains "Project: DetailProject"

  Scenario: Workspace command requires login
    When the user runs the command "tasktracker projects"
    Then the output contains "logged in"
