Feature: HTTP Service Package Organization

  Scenario: ApiClient is in the http package
    Then the class "task.trak.app.client.http.ApiClient" exists

  Scenario: TaskHttpService is in the http package
    Then the class "task.trak.app.client.http.TaskHttpService" exists

  Scenario: ProjectHttpService is in the http package
    Then the class "task.trak.app.client.http.ProjectHttpService" exists

  Scenario: UserHttpService is in the http package
    Then the class "task.trak.app.client.http.UserHttpService" exists

  Scenario: AuthHttpService is in the http package
    Then the class "task.trak.app.client.http.AuthHttpService" exists

  Scenario: SprintHttpService is in the http package
    Then the class "task.trak.app.client.http.SprintHttpService" exists

  Scenario: BacklogHttpService is in the http package
    Then the class "task.trak.app.client.http.BacklogHttpService" exists

  Scenario: ServiceFactory registers HTTP services from new package
    Given HTTP services are registered
    When the task service is requested from ServiceFactory
    Then the returned service class name contains "HttpService"
