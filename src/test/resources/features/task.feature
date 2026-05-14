Feature: Task Management

  Scenario: User adds a task to a project
    Given a user with the username "taskowner" exists
    And a project named "TaskProject" exists
    When the user adds a task with title "Fix bug" to project "TaskProject" assigned to "taskowner"
    Then the system displays task created

  Scenario: User gets a task by ID
    Given a user with the username "taskuser" exists
    And a project named "GetTaskProj" exists
    And a task exists in project "GetTaskProj" assigned to "taskuser"
    When the user runs the command to get the created task
    Then the system displays the task details

  Scenario: User updates task status
    Given a user with the username "statususer" exists
    And a project named "StatusProj" exists
    And a task exists in project "StatusProj" assigned to "statususer"
    When the user runs the command to update the created task with "--status INPROGRESS"
    Then the created task has status "INPROGRESS"

  Scenario: User updates task assignee
    Given a user with the username "origuser" exists
    And a user with the username "newuser" exists
    And a project named "AssignProj" exists
    And a task exists in project "AssignProj" assigned to "origuser"
    When the user runs the command to update the created task with "--assigned_to newuser"
    Then the created task is assigned to "newuser"

  Scenario: User deletes a task
    Given a user with the username "deluser" exists
    And a project named "DelTaskProj" exists
    And a task exists in project "DelTaskProj" assigned to "deluser"
    When the user runs the command to delete the created task
    Then the system asks the user for confirmation
    And the user confirms the task deletion
    Then the created task is deleted

  Scenario: User cannot add task without project
    When the user runs the command "tasktracker task add --title No project task"
    Then the system displays an error containing "project is required"

  Scenario: Task assignee can be changed to project member
    Given a user with the username "assignowner" exists
    And a user with the username "assignmember" exists
    And a project named "AssignTestProj" exists
    And a task exists in project "AssignTestProj" assigned to "assignowner"
    When the user runs the command to update the created task with "--assigned_to assignmember"
    Then the created task is assigned to "assignmember"
