Feature: Project Management

  Scenario: User creates a new project with owner
    Given the command is correctly formatted
    And a user with the username "owner1" exists
    And a project with the name "MyProject" does not already exist
    When the user runs the command "tasktracker project add MyProject --owner owner1"
    Then a new project named "MyProject" is created
    And the project is saved successfully
    And the project "MyProject" has owner "TestUser"

  Scenario: User cannot create a project without an owner
    Given the command is correctly formatted
    And a project with the name "NoOwner" does not already exist
    When the user runs the command "tasktracker project add NoOwner"
    Then the system displays an error containing "owner is required"

  Scenario: User cancels project deletion
    Given a project named "<project_name>" exists
    When the user runs the command "tasktracker project delete <project_name>"
    Then the system asks the user for confirmation
    But the user declines the deletion
    Then the system exits without deleting the project

  Scenario: User confirms project deletion
    Given a project named "<project_name>" exists
    When the user runs the command "tasktracker project delete <project_name>"
    Then the system asks the user for confirmation
    And the user confirms the deletion
    Then the project named "<project_name>" is deleted successfully

  Scenario: User creates a project with a summary
    Given the command is correctly formatted
    And a user with the username "summaryowner" exists
    And a project with the name "SummaryProject" does not already exist
    When the user runs the command "tasktracker project add SummaryProject --owner summaryowner --summary A test summary"
    Then a new project named "SummaryProject" is created
    And the project "SummaryProject" has summary "A test summary"

  Scenario: User creates a project with members by username
    Given the command is correctly formatted
    And a user with the username "projowner" exists
    And a user with the username "alice" exists
    And a user with the username "bob" exists
    And a project with the name "TeamProject" does not already exist
    When the user runs the command "tasktracker project add TeamProject --owner projowner --members [alice,bob]"
    Then a new project named "TeamProject" is created
    And the project "TeamProject" has 2 members

  Scenario: User gets an existing project by ID
    Given a project named "GetProject" exists
    When the user gets project "GetProject" by its ID
    Then the system displays project details "GetProject"

  Scenario: User gets a non-existent project by ID
    When the user runs the command "tasktracker project get 9999999"
    Then the system displays project not found "9999999"

  Scenario: User updates a project name
    Given a project named "OldName" exists
    When the user runs the command "tasktracker project update OldName --name NewName"
    Then the project named "OldName" is renamed to "NewName"

  Scenario: User updates a project summary
    Given a project named "UpdateSummary" exists
    When the user runs the command "tasktracker project update UpdateSummary --summary Updated summary text"
    Then the project "UpdateSummary" has summary "Updated summary text"

  Scenario: User updates project members by username
    Given a project named "UpdateMembers" exists
    And a user with the username "newmember" exists
    When the user runs the command "tasktracker project update UpdateMembers --members [newmember]"
    Then the project "UpdateMembers" has 1 members
