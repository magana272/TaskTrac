Feature: Detail Command with Type Flags

  Scenario: Show sprint details by ID using -s flag
    Given the workspace user "detsprintuser" is logged in
    And a project named "DetSprintProj" exists
    And a sprint named "DetSprint" exists in project "DetSprintProj"
    When the user runs detail with -s flag for sprint "DetSprint"
    Then the output contains "DetSprint"

  Scenario: Show task details by ID using -t flag
    Given the workspace user "dettaskuser" is logged in
    And a project named "DetTaskProj" exists
    And a task exists in project "DetTaskProj" assigned to "dettaskuser"
    When the user runs detail with -t flag for the created task
    Then the output contains "DetTaskProj"

  Scenario: Show project details by ID using -p flag
    Given the workspace user "detprojuser" is logged in
    And a project "DetProjById" exists with owner "detprojuser"
    When the user runs detail with -p flag for project "DetProjById"
    Then the output contains "Project: DetProjById"

  Scenario: Show not found when sprint does not exist
    Given the workspace user "detnotfound" is logged in
    When the user runs the command "tasktracker detail -s 9999999"
    Then the output contains "Not found"

  Scenario: Detail still works without flags
    Given the workspace user "detolduser" is logged in
    And a project "DetOldProject" exists with owner "detolduser"
    When the user runs the command "tasktracker detail DetOldProject"
    Then the output contains "Project: DetOldProject"
