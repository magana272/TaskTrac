Feature: Authentication

  Scenario: User logs in with correct password
    Given a user "authlogin" exists with password "mypassword"
    When the user runs the command "tasktracker login authlogin --password mypassword"
    Then the user "authlogin" is logged in
    And the output contains "Logged in as authlogin"

  Scenario: User logs in with wrong password
    Given a user "authbad" exists with password "correct"
    When the user runs the command "tasktracker login authbad --password wrong"
    Then no user is logged in
    And the output contains "Invalid username or password"

  Scenario: User logs in with non-existent username
    When the user runs the command "tasktracker login nobody --password pass"
    Then no user is logged in
    And the output contains "Invalid username or password"

  Scenario: User logs out
    Given a user "authlogout" exists with password "pass123"
    And the user "authlogout" is currently logged in
    When the user runs the command "tasktracker logout"
    Then no user is logged in
    And the output contains "Logged out"

  Scenario: User signs up via CLI command
    When the user runs the command "tasktracker signup newcliuser --first_name New --last_name CLI --email newcli@example.com --password secret123"
    Then the user "newcliuser" is logged in
    And the output contains "Account created"

  Scenario: User cannot sign up with existing username
    Given a user "existinguser" exists with password "pass"
    When the user runs the command "tasktracker signup existinguser --first_name A --last_name B --email a@b.com --password pass"
    Then the output contains "already exists"

  Scenario: User cannot sign up with duplicate email
    When the user runs the command "tasktracker signup emailuser1 --first_name A --last_name B --email dupe@test.com --password pass"
    When the user runs the command "tasktracker signup emailuser2 --first_name C --last_name D --email dupe@test.com --password pass"
    Then the output contains "already in use"

  Scenario: User signs up via auth service
    Given no user "newsignup" exists
    When the user signs up with username "newsignup" password "secret" first_name "New" last_name "User" email "new@example.com"
    Then the user "newsignup" is logged in
    And the user "newsignup" is saved successfully
