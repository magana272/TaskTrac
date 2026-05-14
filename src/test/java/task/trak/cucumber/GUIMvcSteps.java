package task.trak.cucumber;

import io.cucumber.java.en.*;
import task.trak.api.dto.TaskDTO;
import task.trak.api.dto.request.CreateProjectRequest;
import task.trak.api.dto.request.CreateTaskRequest;
import task.trak.api.dto.request.CreateUserRequest;
import task.trak.api.model.Session;
import task.trak.api.service.ServiceFactory;
import task.trak.app.client.gui.viewmodel.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class GUIMvcSteps {

    private TaskViewModel taskViewModel;
    private ProjectViewModel projectViewModel;
    private SprintViewModel sprintViewModel;
    private UserViewModel userViewModel;
    private final List<ViewModelChangeType> receivedChanges = new ArrayList<>();
    private ViewModelChangeListener listener;

    private TaskDTO task(long id, String project, String title, String status) {
        return new TaskDTO(id, project, "user", title, status, null, null, null, null, null, 0);
    }

    @Given("the GUI model is initialized")
    public void theGuiModelIsInitialized() {
        taskViewModel = new TaskViewModel();
        projectViewModel = new ProjectViewModel();
        sprintViewModel = new SprintViewModel();
        userViewModel = new UserViewModel();
        receivedChanges.clear();
    }

    @Given("local services are registered")
    public void localServicesAreRegistered() {
        ServiceFactory.registerLocalServices();
    }

    @Given("a test user {string} exists")
    public void aTestUserExists(String username) {
        var userService = ServiceFactory.userService();
        if (userService.getByUsername(username) == null) {
            userService.create(new CreateUserRequest(username, "Test", "User", username + "@test.com", "password"));
        }
    }

    @Given("a test project {string} exists with owner {string}")
    public void aTestProjectExistsWithOwner(String projectName, String owner) {
        var projectService = ServiceFactory.projectService();
        if (projectService.getByName(projectName) == null) {
            projectService.create(new CreateProjectRequest(projectName, "Test project", owner, List.of()));
        }
    }

    @Given("a test task {string} exists in project {string} assigned to {string}")
    public void aTestTaskExistsInProject(String title, String project, String assignee) {
        var taskService = ServiceFactory.taskService();
        taskService.create(new CreateTaskRequest(title, project, assignee, "Test task", null, null));
    }

    @When("the controller loads tasks")
    public void theControllerLoadsTasks() {
        var taskService = ServiceFactory.taskService();
        taskViewModel.setAll(taskService.listAll());
    }

    @Then("the model contains at least {int} task")
    public void theModelContainsAtLeastNTasks(int count) {
        assertTrue("Expected at least " + count + " tasks, got " + taskViewModel.get().size(),
                taskViewModel.get().size() >= count);
    }

    @Given("a model change listener is registered")
    public void aModelChangeListenerIsRegistered() {
        receivedChanges.clear();
        listener = receivedChanges::add;
        taskViewModel.addObserver(listener);
        projectViewModel.addObserver(listener);
        sprintViewModel.addObserver(listener);
        userViewModel.addObserver(listener);
    }

    @When("the model tasks are updated")
    public void theModelTasksAreUpdated() {
        taskViewModel.setAll(List.of(task(1, "P", "T1", "READY")));
    }

    @Then("the listener receives a TASKS change notification")
    public void theListenerReceivesTasksChange() {
        assertTrue(receivedChanges.contains(ViewModelChangeType.TASKS));
    }

    @When("the model projects are updated")
    public void theModelProjectsAreUpdated() {
        projectViewModel.setAll(List.of());
    }

    @Then("the listener receives a PROJECTS change notification")
    public void theListenerReceivesProjectsChange() {
        assertTrue(receivedChanges.contains(ViewModelChangeType.PROJECTS));
    }

    @When("the model sprints are updated")
    public void theModelSprintsAreUpdated() {
        sprintViewModel.setAll(List.of());
    }

    @Then("the listener receives a SPRINTS change notification")
    public void theListenerReceivesSprintsChange() {
        assertTrue(receivedChanges.contains(ViewModelChangeType.SPRINTS));
    }

    @When("the model session is set for user {string}")
    public void theModelSessionIsSetForUser(String username) {
        userViewModel.setSession(new Session(username));
    }

    @Then("the model session has username {string}")
    public void theModelSessionHasUsername(String username) {
        assertNotNull(userViewModel.getSession());
        assertEquals(username, userViewModel.getSession().getLogged_in_user());
    }

    @Then("the listener receives a SESSION change notification")
    public void theListenerReceivesSessionChange() {
        assertTrue(receivedChanges.contains(ViewModelChangeType.SESSION));
    }

    @Given("the model has tasks with statuses {string} {string} {string}")
    public void theModelHasTasksWithStatuses(String s1, String s2, String s3) {
        taskViewModel.setAll(List.of(
                task(1, "P", "Task1", s1),
                task(2, "P", "Task2", s2),
                task(3, "P", "Task3", s3)
        ));
    }

    @When("show completed is set to false")
    public void showCompletedSetToFalse() {
        taskViewModel.setShowCompleted(false);
    }

    @When("show completed is set to true")
    public void showCompletedSetToTrue() {
        taskViewModel.setShowCompleted(true);
    }

    @Then("the filtered tasks do not include status {string}")
    public void theFilteredTasksDoNotIncludeStatus(String status) {
        for (TaskDTO t : taskViewModel.getFiltered()) {
            assertNotEquals(status, t.status());
        }
    }

    @Then("the filtered tasks include status {string}")
    public void theFilteredTasksIncludeStatus(String status) {
        boolean found = taskViewModel.getFiltered().stream().anyMatch(t -> status.equals(t.status()));
        assertTrue("Expected to find status " + status + " in filtered tasks", found);
    }

    @Given("the model has tasks with different deadlines")
    public void theModelHasTasksWithDifferentDeadlines() {
        taskViewModel.setShowCompleted(true);
        taskViewModel.setAll(List.of(
                new TaskDTO(1L, "P", "u", "Late", "READY", null, null, null, new Date(9000000), null, 0),
                new TaskDTO(2L, "P", "u", "Early", "READY", null, null, null, new Date(1000000), null, 0)
        ));
    }

    @When("task sort is set to {string}")
    public void taskSortIsSetTo(String sort) {
        taskViewModel.setSort(sort);
    }

    @Then("the filtered tasks are sorted by deadline ascending")
    public void theFilteredTasksAreSortedByDeadlineAscending() {
        List<TaskDTO> filtered = taskViewModel.getFiltered();
        assertTrue(filtered.size() >= 2);
        assertEquals("Early", filtered.get(0).title());
        assertEquals("Late", filtered.get(1).title());
    }

    @Given("the model has tasks in projects {string} and {string}")
    public void theModelHasTasksInProjects(String p1, String p2) {
        taskViewModel.setShowCompleted(true);
        taskViewModel.setAll(List.of(
                task(1, p1, "T1", "READY"),
                task(2, p2, "T2", "READY")
        ));
    }

    @When("task project filter is set to {string}")
    public void taskProjectFilterIsSetTo(String project) {
        taskViewModel.setProjectFilter(project);
    }

    @Then("the filtered tasks only contain project {string}")
    public void theFilteredTasksOnlyContainProject(String project) {
        for (TaskDTO t : taskViewModel.getFiltered()) {
            assertEquals(project, t.projectName());
        }
        assertFalse("Expected at least one task", taskViewModel.getFiltered().isEmpty());
    }

    @When("the listener is removed")
    public void theListenerIsRemoved() {
        taskViewModel.removeObserver(listener);
        projectViewModel.removeObserver(listener);
        sprintViewModel.removeObserver(listener);
        userViewModel.removeObserver(listener);
        receivedChanges.clear();
    }

    @Then("the listener receives no change notification")
    public void theListenerReceivesNoChangeNotification() {
        assertTrue("Expected no notifications after removal", receivedChanges.isEmpty());
    }
}
