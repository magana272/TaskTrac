Feature: GUI MVC Architecture

  Scenario: Model holds task state after controller loads data
    Given the GUI model is initialized
    And local services are registered
    And a test user "mvcuser" exists
    And a test project "MVCProject" exists with owner "mvcuser"
    And a test task "MVC Task 1" exists in project "MVCProject" assigned to "mvcuser"
    When the controller loads tasks
    Then the model contains at least 1 task

  Scenario: Model notifies listeners on task change
    Given the GUI model is initialized
    And a model change listener is registered
    When the model tasks are updated
    Then the listener receives a TASKS change notification

  Scenario: Model notifies listeners on project change
    Given the GUI model is initialized
    And a model change listener is registered
    When the model projects are updated
    Then the listener receives a PROJECTS change notification

  Scenario: Model notifies listeners on sprint change
    Given the GUI model is initialized
    And a model change listener is registered
    When the model sprints are updated
    Then the listener receives a SPRINTS change notification

  Scenario: Model holds session state
    Given the GUI model is initialized
    When the model session is set for user "testuser"
    Then the model session has username "testuser"

  Scenario: Model notifies on session change
    Given the GUI model is initialized
    And a model change listener is registered
    When the model session is set for user "testuser"
    Then the listener receives a SESSION change notification

  Scenario: Model filter excludes completed tasks
    Given the GUI model is initialized
    And the model has tasks with statuses "READY" "INPROGRESS" "COMPLETE"
    When show completed is set to false
    Then the filtered tasks do not include status "COMPLETE"

  Scenario: Model filter includes completed tasks when enabled
    Given the GUI model is initialized
    And the model has tasks with statuses "READY" "INPROGRESS" "COMPLETE"
    When show completed is set to true
    Then the filtered tasks include status "COMPLETE"

  Scenario: Model sort by due date
    Given the GUI model is initialized
    And the model has tasks with different deadlines
    When task sort is set to "Due Date"
    Then the filtered tasks are sorted by deadline ascending

  Scenario: Model filter by project name
    Given the GUI model is initialized
    And the model has tasks in projects "Alpha" and "Beta"
    When task project filter is set to "Alpha"
    Then the filtered tasks only contain project "Alpha"

  Scenario: Removed listener does not receive notifications
    Given the GUI model is initialized
    And a model change listener is registered
    When the listener is removed
    And the model tasks are updated
    Then the listener receives no change notification
