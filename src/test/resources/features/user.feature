Feature: User Management


  Scenario: User creates a new user
    Given a user with the username "jdoe" does not already exist
    When the user runs the command "tasktracker user add jdoe --first_name Jane --last_name Doe --email jane@example.com"
    Then a new user named "jdoe" is created
    And the user "jdoe" is saved successfully

  Scenario: User creates a new user with minimal info
    Given a user with the username "mmagana" does not already exist
    When the user runs the command "tasktracker user add mmagana --first_name Manuel --last_name Magana"
    Then a new user named "mmagana" is created
    And the user "mmagana" is saved successfully


  Scenario: User gets an existing user
    Given a user with the username "getme" exists
    When the user runs the command "tasktracker user get getme"
    Then the system displays user "getme"

  Scenario: User gets a non-existent user
    Given a user with the username "ghost" does not already exist
    When the user runs the command "tasktracker user get ghost"
    Then the system displays user not found "ghost"


  Scenario: User updates email
    Given a user with the username "updateme" exists
    When the user runs the command "tasktracker user update updateme --email new@example.com"
    Then the user "updateme" has email "new@example.com"

  Scenario: User updates first and last name
    Given a user with the username "rename" exists
    When the user runs the command "tasktracker user update rename --first_name Alice --last_name Wonder"
    Then the user "rename" has first name "Alice"
    And the user "rename" has last name "Wonder"


  Scenario: User deletes an existing user
    Given a user with the username "deleteme" exists
    When the user runs the command "tasktracker user delete deleteme"
    Then the system asks the user for confirmation
    And the user confirms the deletion
    Then the user "deleteme" is deleted successfully

  Scenario: User cancels user deletion
    Given a user with the username "keepme" exists
    When the user runs the command "tasktracker user delete keepme"
    Then the system asks the user for confirmation
    But the user declines the deletion
    Then the user "keepme" still exists
