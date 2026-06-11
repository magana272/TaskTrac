Feature: Authentication

  Scenario: User logs in with correct password
    Given a user "authlogin" exists with password "MyPass1!"
    When the user runs the command "tasktracker login authlogin --password MyPass1!"
    Then the user "authlogin" is logged in
    And the output contains "Logged in as authlogin"

  Scenario: User logs in with wrong password
    Given a user "authbad" exists with password "Right1!"
    When the user runs the command "tasktracker login authbad --password Wrong1!"
    Then no user is logged in
    And the output contains "Invalid username or password"

  Scenario: User logs in with non-existent username
    When the user runs the command "tasktracker login nobody --password Pass1!"
    Then no user is logged in
    And the output contains "Invalid username or password"

  Scenario: User logs out
    Given a user "authlogout" exists with password "Pass1!"
    And the user "authlogout" is currently logged in
    When the user runs the command "tasktracker logout"
    Then no user is logged in
    And the output contains "Logged out"

  Scenario: User signs up via CLI command
    When the user runs the command "tasktracker signup newcliuser --first_name New --last_name CLI --email newcli@example.com --password Secret1!"
    Then the user "newcliuser" is logged in
    And the output contains "Account created"

  Scenario: User cannot sign up with existing username
    Given a user "existinguser" exists with password "Pass1!"
    When the user runs the command "tasktracker signup existinguser --first_name A --last_name B --email a@b.com --password Pass1!"
    Then the output contains "already exists"

  Scenario: User cannot sign up with duplicate email
    When the user runs the command "tasktracker signup emailuser1 --first_name A --last_name B --email dupe@test.com --password Pass1!"
    When the user runs the command "tasktracker signup emailuser2 --first_name C --last_name D --email dupe@test.com --password Pass1!"
    Then the output contains "already in use"

  Scenario: User signs up via auth service
    Given no user "newsignup" exists
    When the user signs up with username "newsignup" password "Secret1!" first_name "New" last_name "User" email "new@example.com"
    Then the user "newsignup" is logged in
    And the user "newsignup" is saved successfully

  Scenario: Google sign-in creates new user when email not found
    Given no user with email "googleuser@gmail.com" exists
    When a Google sign-in is completed with email "googleuser@gmail.com" name "Google" "User"
    Then a user with email "googleuser@gmail.com" is logged in

  Scenario: Google sign-in matches existing user by email
    Given a user "existgoogle" exists with password "Pass1!" and email "existing@gmail.com"
    When a Google sign-in is completed with email "existing@gmail.com" name "Existing" "User"
    Then the user "existgoogle" is logged in

  Scenario: Google sign-in rejects invalid token
    When a Google sign-in is attempted with an invalid token
    Then the Google sign-in is rejected

  Scenario: Logout clears all view data
    Given a user "viewclearuser" exists with password "Pass1!"
    And the user "viewclearuser" is currently logged in
    When the user runs the command "tasktracker logout"
    Then no user is logged in
    And the output contains "Logged out"

  Scenario: User requests password reset with valid email
    Given a user "resetuser" exists with password "OldPass1!"
    When the user requests a password reset for email "resetuser@example.com"
    Then the output contains "reset code has been sent"

  Scenario: User resets password with valid code
    Given a user "resetuser2" exists with password "OldPass1!"
    And a password reset code exists for user "resetuser2"
    When the user resets the password with the code and new password "NewPass1!"
    Then the user "resetuser2" can authenticate with password "NewPass1!"
    And the user "resetuser2" cannot authenticate with password "OldPass1!"

  Scenario: User cannot reset with invalid code
    When the user runs the command "tasktracker reset-password --code 000000 --password NewPass1!"
    Then the system displays an error containing "Invalid or expired"

  Scenario: Reset code is single-use
    Given a user "singleuse" exists with password "OldPass1!"
    And a password reset code exists for user "singleuse"
    When the user resets the password with the code and new password "NewPass1!"
    Then the same reset code cannot be used again
