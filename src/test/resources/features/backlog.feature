Feature: Backlog Management

  Scenario: User creates a backlog for a project
    Given a project named "BacklogProj" exists
    When the user runs the command "tasktracker backlog add MainBacklog --project BacklogProj"
    Then the system displays backlog created "MainBacklog"

  Scenario: User gets a backlog
    Given a project named "GetBLProj" exists
    And a backlog named "GetBacklog" exists in project "GetBLProj"
    When the user runs the command "tasktracker backlog get GetBacklog"
    Then the system displays backlog details "GetBacklog"

  Scenario: User gets a non-existent backlog
    When the user runs the command "tasktracker backlog get NoSuchBacklog"
    Then the system displays backlog not found "NoSuchBacklog"

  Scenario: User adds a task to a backlog
    Given a project named "AddTaskBLProj" exists
    And a backlog named "TaskBacklog" exists in project "AddTaskBLProj"
    When the user runs the command "tasktracker backlog update TaskBacklog --add_task 12345"
    Then the backlog "TaskBacklog" has 1 tasks

  Scenario: User removes a task from a backlog
    Given a project named "RemTaskBLProj" exists
    And a backlog named "RemBacklog" exists in project "RemTaskBLProj" with task 99999
    When the user runs the command "tasktracker backlog update RemBacklog --remove_task 99999"
    Then the backlog "RemBacklog" has 0 tasks

  Scenario: User deletes a backlog
    Given a project named "DelBLProj" exists
    And a backlog named "DelBacklog" exists in project "DelBLProj"
    When the user runs the command "tasktracker backlog delete DelBacklog"
    Then the system asks the user for confirmation
    And the user confirms the deletion
    Then the backlog "DelBacklog" is deleted

  Scenario: User cannot create backlog without project
    When the user runs the command "tasktracker backlog add OrphanBacklog"
    Then the system displays an error containing "project is required"
