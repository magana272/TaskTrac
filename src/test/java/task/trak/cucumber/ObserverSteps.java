package task.trak.cucumber;

import io.cucumber.java.en.*;
import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.model.Session;
import task.trak.app.client.gui.viewmodel.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ObserverSteps {

    private TaskViewModel taskViewModel;
    private ProjectViewModel projectViewModel;
    private UserViewModel userViewModel;
    private final List<ViewModelChangeType> received = new ArrayList<>();
    private final List<ViewModelChangeType> received2 = new ArrayList<>();
    private ViewModelChangeListener observer;
    private final List<String> seenDuringNotification = new ArrayList<>();

    @Given("a TaskViewModel is initialized")
    public void aTaskViewModelIsInitialized() {
        taskViewModel = new TaskViewModel();
        received.clear();
    }

    @Given("a ProjectViewModel is initialized")
    public void aProjectViewModelIsInitialized() {
        projectViewModel = new ProjectViewModel();
        received.clear();
        received2.clear();
    }

    @Given("an observer is registered on the TaskViewModel")
    public void anObserverIsRegisteredOnTheTaskViewModel() {
        observer = received::add;
        taskViewModel.addObserver(observer);
    }

    @Given("an observer is registered on the ProjectViewModel")
    public void anObserverIsRegisteredOnTheProjectViewModel() {
        observer = received::add;
        projectViewModel.addObserver(observer);
    }

    @Given("two observers are registered on the ProjectViewModel")
    public void twoObserversAreRegisteredOnTheProjectViewModel() {
        projectViewModel.addObserver(received::add);
        projectViewModel.addObserver(received2::add);
    }

    @Given("an observer that reads data is registered on the ProjectViewModel")
    public void anObserverThatReadsDataIsRegistered() {
        projectViewModel.addObserver(type -> {
            for (var p : projectViewModel.get()) {
                seenDuringNotification.add(p.projectName());
            }
        });
    }

    @When("tasks are updated in the TaskViewModel")
    public void tasksAreUpdatedInTheTaskViewModel() {
        taskViewModel.setAll(List.of(
                new TaskDTO(1L, "P", "u", "T1", "READY", null, null, null, null, null, 0)));
    }

    @When("the observer is removed from the TaskViewModel")
    public void theObserverIsRemovedFromTheTaskViewModel() {
        taskViewModel.removeObserver(observer);
        received.clear();
    }

    @When("a project is created in the ProjectViewModel")
    public void aProjectIsCreatedInTheProjectViewModel() {
        projectViewModel.create(
                new ProjectDTO(1L, "TestProject", "desc", null, "owner", List.of(), 0, 0, 0));
    }

    @When("projects are updated in the ProjectViewModel")
    public void projectsAreUpdatedInTheProjectViewModel() {
        projectViewModel.setAll(List.of());
    }

    @When("a project named {string} is created")
    public void aProjectNamedIsCreated(String name) {
        projectViewModel.create(
                new ProjectDTO(1L, name, "desc", null, "owner", List.of(), 0, 0, 0));
    }

    @Then("the observer receives a TASKS notification")
    public void theObserverReceivesATasksNotification() {
        assertTrue("Expected TASKS notification", received.contains(ViewModelChangeType.TASKS));
    }

    @Then("the observer receives no notification")
    public void theObserverReceivesNoNotification() {
        assertTrue("Expected no notifications", received.isEmpty());
    }

    @Then("the observer receives a PROJECTS notification")
    public void theObserverReceivesAProjectsNotification() {
        assertTrue("Expected PROJECTS notification", received.contains(ViewModelChangeType.PROJECTS));
    }

    @Then("the ProjectViewModel contains the new project")
    public void theProjectViewModelContainsTheNewProject() {
        assertFalse("Expected at least one project", projectViewModel.get().isEmpty());
        assertEquals("TestProject", projectViewModel.get().get(0).projectName());
    }

    @Then("both observers receive a PROJECTS notification")
    public void bothObserversReceiveAProjectsNotification() {
        assertTrue("Observer 1 should be notified", received.contains(ViewModelChangeType.PROJECTS));
        assertTrue("Observer 2 should be notified", received2.contains(ViewModelChangeType.PROJECTS));
    }

    @Then("the observer saw {string} during notification")
    public void theObserverSawDuringNotification(String name) {
        assertTrue("Observer should have seen '" + name + "' during notification",
                seenDuringNotification.contains(name));
    }

    // --- UserViewModel steps ---

    @Given("a UserViewModel with a session")
    public void aUserViewModelWithASession() {
        userViewModel = new UserViewModel();
        userViewModel.setSession(new Session("testuser"));
        received.clear();
    }

    @Given("a UserViewModel is initialized")
    public void aUserViewModelIsInitialized() {
        userViewModel = new UserViewModel();
        received.clear();
    }

    @Given("an observer is registered on the UserViewModel")
    public void anObserverIsRegisteredOnTheUserViewModel() {
        observer = received::add;
        userViewModel.addObserver(observer);
    }

    @When("the session is set to null")
    public void theSessionIsSetToNull() {
        userViewModel.setSession(null);
    }

    @When("output is set to {string}")
    public void outputIsSetTo(String text) {
        userViewModel.setOutput(text);
    }

    @Then("the observer receives a SESSION notification")
    public void theObserverReceivesASessionNotification() {
        assertTrue("Expected SESSION notification", received.contains(ViewModelChangeType.SESSION));
    }

    @Then("the session is null")
    public void theSessionIsNull() {
        assertNull("Session should be null", userViewModel.getSession());
    }

    @Then("the observer receives an OUTPUT notification")
    public void theObserverReceivesAnOutputNotification() {
        assertTrue("Expected OUTPUT notification", received.contains(ViewModelChangeType.OUTPUT));
    }
}
