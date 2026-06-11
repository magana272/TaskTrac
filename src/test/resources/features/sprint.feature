Feature: Sprint Management

  Scenario: User creates a sprint for a project
    Given a project named "SprintProj" exists
    When the user runs the command "tasktracker sprint add Sprint1 --project SprintProj"
    Then the system displays sprint created "Sprint1"

  Scenario: User gets a sprint by ID
    Given a project named "GetSprintProj" exists
    And a sprint named "GetSprint" exists in project "GetSprintProj"
    When the user gets sprint "GetSprint" by its ID
    Then the system displays sprint details "GetSprint"

  Scenario: User gets a non-existent sprint by ID
    When the user runs the command "tasktracker sprint get 9999999"
    Then the system displays sprint not found "9999999"

  Scenario: User updates sprint dates
    Given a project named "DateProj" exists
    And a sprint named "DateSprint" exists in project "DateProj"
    When the user runs the command "tasktracker sprint update DateSprint --project DateProj --start_date 2026-06-01 --end_date 2026-06-14"
    Then the sprint "DateSprint" has start date "2026-06-01"

  Scenario: User deletes a sprint
    Given a project named "DelSprintProj" exists
    And a sprint named "DelSprint" exists in project "DelSprintProj"
    When the user runs the command "tasktracker sprint delete DelSprint"
    Then the system asks the user for confirmation
    And the user confirms the deletion
    Then the sprint "DelSprint" is deleted

  Scenario: User cannot create sprint without project
    When the user runs the command "tasktracker sprint add OrphanSprint"
    Then the system displays an error containing "project is required"

  Scenario: User completes a sprint with a review
    Given a project named "ReviewProj" exists
    And a sprint named "ReviewSprint" exists in project "ReviewProj"
    When the user runs the command "tasktracker sprint update ReviewSprint --project ReviewProj --complete --review Good sprint overall"
    Then the sprint "ReviewSprint" is completed
    And the sprint "ReviewSprint" has review "Good sprint overall"

  Scenario: User cannot complete a sprint without a review
    Given a project named "NoReviewProj" exists
    And a sprint named "NoReviewSprint" exists in project "NoReviewProj"
    When the user runs the command "tasktracker sprint update NoReviewSprint --project NoReviewProj --complete"
    Then the system displays an error containing "Review is required"

  Scenario: User cannot complete a sprint with a review longer than 300 characters
    Given a project named "LongReviewProj" exists
    And a sprint named "LongReviewSprint" exists in project "LongReviewProj"
    When the user runs the command "tasktracker sprint update LongReviewSprint --project LongReviewProj --complete --review aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    Then the system displays an error containing "300 characters"
