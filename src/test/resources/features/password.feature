Feature: Password Management

  Scenario: User creates account with password
    Given a user with the username "pwduser" does not already exist
    When the user runs the command "tasktracker user add pwduser --first_name Pass --last_name User --password Secret1!"
    Then a new user named "pwduser" is created
    And the user "pwduser" has a password set

  Scenario: User updates password
    Given a user "pwdupdate" exists with password "OldPw1!"
    When the user runs the command "tasktracker user update pwdupdate --password NewPw1!"
    Then the user "pwdupdate" can authenticate with password "NewPw1!"
    And the user "pwdupdate" cannot authenticate with password "OldPw1!"

  Scenario: User created without password has no password hash
    Given a user with the username "nopwd" does not already exist
    When the user runs the command "tasktracker user add nopwd --first_name No --last_name Password"
    Then a new user named "nopwd" is created
    And the user "nopwd" has no password set
