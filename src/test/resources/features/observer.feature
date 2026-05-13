Feature: Observer Pattern for ViewModels

  Scenario: Observer receives notification when ViewModel data changes
    Given a TaskViewModel is initialized
    And an observer is registered on the TaskViewModel
    When tasks are updated in the TaskViewModel
    Then the observer receives a TASKS notification

  Scenario: Removed observer does not receive notifications
    Given a TaskViewModel is initialized
    And an observer is registered on the TaskViewModel
    When the observer is removed from the TaskViewModel
    And tasks are updated in the TaskViewModel
    Then the observer receives no notification

  Scenario: Cross-domain observer receives notification
    Given a ProjectViewModel is initialized
    And an observer is registered on the ProjectViewModel
    When a project is created in the ProjectViewModel
    Then the observer receives a PROJECTS notification
    And the ProjectViewModel contains the new project

  Scenario: Multiple observers all receive notifications
    Given a ProjectViewModel is initialized
    And two observers are registered on the ProjectViewModel
    When projects are updated in the ProjectViewModel
    Then both observers receive a PROJECTS notification

  Scenario: Observer sees fresh data during notification
    Given a ProjectViewModel is initialized
    And an observer that reads data is registered on the ProjectViewModel
    When a project named "FreshProject" is created
    Then the observer saw "FreshProject" during notification
