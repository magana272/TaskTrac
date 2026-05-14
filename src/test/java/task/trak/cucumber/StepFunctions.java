package task.trak.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import task.trak.Main;
import task.trak.api.model.Session;
import task.trak.api.service.AuthService;
import task.trak.api.service.UserService;
import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.dao.SessionDAO;
import task.trak.app.server.model.backlog.BackLog;
import task.trak.app.server.model.project.Project;
import task.trak.app.server.model.sprint.Sprint;
import task.trak.app.server.model.task.Task;
import task.trak.app.server.model.user.User;
import task.trak.app.server.service.auth.TrakAuthService;
import task.trak.app.server.service.user.TrakUserService;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class StepFunctions {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private String consoleOutput;
    private String pendingCommand;
    private String projectName;
    private Long createdTaskId;

    @Before
    public void setUp() {
        File storeDir = new File(TTApp.storedir);
        if (storeDir.exists()) {
            File[] files = storeDir.listFiles();
            if (files != null) {
                for (File f : files) f.delete();
            }
        }
    }

    @After
    public void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        System.setErr(originalErr);
        SessionDAO.clear();
    }

    // --- Create scenario steps ---

    @Given("the command is correctly formatted")
    public void theCommandIsCorrectlyFormatted() {
        assertTrue(true);
    }

    @And("a project with the name {string} does not already exist")
    public void aProjectWithTheNameDoesNotAlreadyExist(String name) {
        this.projectName = name;
        File file = new File(TTApp.storedir + File.separator + name + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    @When("the user runs the command {string}")
    public void theUserRunsTheCommand(String command) {
        String[] tokens = tokenize(command);
        // Strip "trak" prefix
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

        if (command.contains("delete")) {
            // Defer execution for delete commands — stdin needed from confirm/decline step
            this.pendingCommand = command;
        } else {
            executeCommand(args, null);
        }
    }

    @Then("a new project named {string} is created")
    public void aNewProjectNamedIsCreated(String name) {
        this.projectName = name;
        assertTrue("Expected output to confirm project creation",
                consoleOutput.contains(name) && consoleOutput.contains("created"));
    }

    @And("the project is saved successfully")
    public void theProjectIsSavedSuccessfully() {
        Project loaded = DAOFactory.projectDAO().loadByKey(this.projectName);
        assertNotNull("Expected project to be saved: " + this.projectName, loaded);
    }

    // --- Delete scenario steps ---

    @Given("a project named {string} exists")
    public void aProjectNamedExists(String name) {
        this.projectName = name;
        // Ensure .store directory exists
        File storeDir = new File(TTApp.storedir);
        if (!storeDir.exists()) {
            storeDir.mkdirs();
        }
        // Create a default owner user, then create the project with that owner
        executeCommand(new String[]{"user", "add", "defaultowner", "--first_name", "Default", "--last_name", "Owner"}, null);
        executeCommand(new String[]{"project", "add", name, "--owner", "defaultowner"}, null);
        Project loaded = DAOFactory.projectDAO().loadByKey(name);
        assertNotNull("Project should exist after creation", loaded);
    }

    @Then("the system displays an error containing {string}")
    public void theSystemDisplaysAnErrorContaining(String text) {
        assertTrue("Expected error containing '" + text + "' but got: " + consoleOutput,
                consoleOutput.toLowerCase().contains(text.toLowerCase()));
    }

    @Then("the system asks the user for confirmation")
    public void theSystemAsksTheUserForConfirmation() {
        // Verified retroactively after command executes in the confirm/decline step
    }

    @But("the user declines the deletion")
    public void theUserDeclinesTheDeletion() {
        assertNotNull("Expected a pending delete command", this.pendingCommand);
        String[] tokens = this.pendingCommand.split(" ");
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
        executeCommand(args, "no\n");
        assertTrue("Expected confirmation prompt in output",
                consoleOutput.contains("Confirm") || consoleOutput.contains("yes/no"));
    }

    @Then("the system exits without deleting the project")
    public void theSystemExitsWithoutDeletingTheProject() {
        Project loaded = DAOFactory.projectDAO().loadByKey(this.projectName);
        assertNotNull("Project should still exist after declined deletion", loaded);
        assertTrue("Expected cancellation message", consoleOutput.contains("cancelled"));
    }

    @And("the user confirms the deletion")
    public void theUserConfirmsTheDeletion() {
        assertNotNull("Expected a pending delete command", this.pendingCommand);
        String[] tokens = this.pendingCommand.split(" ");
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
        executeCommand(args, "yes\n");
    }

    @Then("the project named {string} is deleted successfully")
    public void theProjectNamedIsDeletedSuccessfully(String name) {
        Project loaded = DAOFactory.projectDAO().loadByKey(name);
        assertNull("Project should be deleted", loaded);
        assertTrue("Expected deletion success message",
                consoleOutput.contains("deleted"));
    }


    // --- Get steps ---

    @Then("the system displays project details {string}")
    public void theSystemDisplaysProjectDetails(String name) {
        assertTrue("Expected project name in output", consoleOutput.contains(name));
    }

    @Then("the system displays project not found {string}")
    public void theSystemDisplaysProjectNotFound(String name) {
        assertTrue("Expected not found message", consoleOutput.contains("not found"));
    }

    // --- Add/Update with options steps ---

    @And("the project {string} has summary {string}")
    public void theProjectHasSummary(String name, String summary) {
        Project loaded = DAOFactory.projectDAO().loadByKey(name);
        assertNotNull("Project should exist", loaded);
        assertEquals(summary, loaded.getSummary());
    }

    @And("the project {string} has owner {string}")
    public void theProjectHasOwner(String name, String ownerName) {
        Project loaded = DAOFactory.projectDAO().loadByKey(name);
        assertNotNull("Project should exist", loaded);
        assertNotNull("Owner should exist", loaded.getOwner());
        assertEquals(ownerName, loaded.getOwner().getName());
    }

    @Then("the project named {string} is renamed to {string}")
    public void theProjectIsRenamedTo(String oldName, String newName) {
        this.projectName = newName;
        assertNull("Old project should be deleted", DAOFactory.projectDAO().loadByKey(oldName));
        assertNotNull("New project should exist", DAOFactory.projectDAO().loadByKey(newName));
        assertTrue("Expected update message", consoleOutput.contains("updated"));
    }

    @And("the project {string} has {int} members")
    public void theProjectHasMembers(String name, int count) {
        Project loaded = DAOFactory.projectDAO().loadByKey(name);
        assertNotNull("Project should exist", loaded);
        assertNotNull("Members should exist", loaded.getMembers());
        assertEquals(count, loaded.getMembers().size());
    }

    // --- User Management steps ---

    @Given("a user with the username {string} does not already exist")
    public void aUserWithTheUsernameDoesNotAlreadyExist(String username) {
        DAOFactory.userDAO().deleteByKey(username);
    }

    @Given("a user with the username {string} exists")
    public void aUserWithTheUsernameExists(String username) {
        File storeDir = new File(TTApp.storedir);
        if (!storeDir.exists()) {
            storeDir.mkdirs();
        }
        executeCommand(new String[]{"user", "add", username, "--first_name", "Test", "--last_name", "User"}, null);
        User loaded = DAOFactory.userDAO().loadByKey(username);
        assertNotNull("User should exist after creation", loaded);
    }

    @Then("a new user named {string} is created")
    public void aNewUserNamedIsCreated(String username) {
        assertTrue("Expected output to confirm user creation",
                consoleOutput.contains(username) && consoleOutput.contains("created"));
    }

    @And("the user {string} is saved successfully")
    public void theUserIsSavedSuccessfully(String username) {
        User loaded = DAOFactory.userDAO().loadByKey(username);
        assertNotNull("Expected user to be saved: " + username, loaded);
    }

    @Then("the system displays user {string}")
    public void theSystemDisplaysUser(String username) {
        assertTrue("Expected output to contain username", consoleOutput.contains(username));
    }

    @Then("the system displays user not found {string}")
    public void theSystemDisplaysUserNotFound(String username) {
        assertTrue("Expected not found message", consoleOutput.contains("not found"));
    }

    @Then("the user {string} has email {string}")
    public void theUserHasEmail(String username, String email) {
        User loaded = DAOFactory.userDAO().loadByKey(username);
        assertNotNull("User should exist", loaded);
        assertEquals(email, loaded.getEmail());
    }

    @Then("the user {string} has first name {string}")
    public void theUserHasFirstName(String username, String firstName) {
        User loaded = DAOFactory.userDAO().loadByKey(username);
        assertNotNull("User should exist", loaded);
        assertEquals(firstName, loaded.getFirst_name());
    }

    @And("the user {string} has last name {string}")
    public void theUserHasLastName(String username, String lastName) {
        User loaded = DAOFactory.userDAO().loadByKey(username);
        assertNotNull("User should exist", loaded);
        assertEquals(lastName, loaded.getLast_name());
    }

    @Then("the user {string} is deleted successfully")
    public void theUserIsDeletedSuccessfully(String username) {
        User loaded = DAOFactory.userDAO().loadByKey(username);
        assertNull("User should be deleted", loaded);
        assertTrue("Expected deletion message", consoleOutput.contains("deleted"));
    }

    @Then("the user {string} still exists")
    public void theUserStillExists(String username) {
        User loaded = DAOFactory.userDAO().loadByKey(username);
        assertNotNull("User should still exist", loaded);
        assertTrue("Expected cancellation message", consoleOutput.contains("cancelled"));
    }

    // --- Backlog Management steps ---

    @And("a backlog named {string} exists in project {string}")
    public void aBacklogNamedExistsInProject(String backlogName, String projectName) {
        executeCommand(new String[]{"backlog", "add", backlogName, "--project", projectName}, null);
        BackLog loaded = DAOFactory.backlogDAO().loadByKey(backlogName);
        assertNotNull("Backlog should exist after creation", loaded);
    }

    @And("a backlog named {string} exists in project {string} with task {long}")
    public void aBacklogNamedExistsInProjectWithTask(String backlogName, String projectName, Long taskId) {
        executeCommand(new String[]{"backlog", "add", backlogName, "--project", projectName}, null);
        BackLog loaded = DAOFactory.backlogDAO().loadByKey(backlogName);
        assertNotNull("Backlog should exist after creation", loaded);
        executeCommand(new String[]{"backlog", "update", backlogName, "--add_task", String.valueOf(taskId)}, null);
    }

    @Then("the system displays backlog created {string}")
    public void theSystemDisplaysBacklogCreated(String name) {
        assertTrue("Expected backlog created message",
                consoleOutput.contains(name) && consoleOutput.contains("created"));
    }

    @Then("the system displays backlog details {string}")
    public void theSystemDisplaysBacklogDetails(String name) {
        assertTrue("Expected backlog details", consoleOutput.contains(name));
    }

    @Then("the system displays backlog not found {string}")
    public void theSystemDisplaysBacklogNotFound(String name) {
        assertTrue("Expected not found message", consoleOutput.contains("not found"));
    }

    @Then("the backlog {string} has {int} tasks")
    public void theBacklogHasTasks(String name, int count) {
        BackLog loaded = DAOFactory.backlogDAO().loadByKey(name);
        assertNotNull("Backlog should exist", loaded);
        assertEquals(count, loaded.getTask_ids().size());
    }

    @Then("the backlog {string} is deleted")
    public void theBacklogIsDeleted(String name) {
        BackLog loaded = DAOFactory.backlogDAO().loadByKey(name);
        assertNull("Backlog should be deleted", loaded);
        assertTrue("Expected deletion message", consoleOutput.contains("deleted"));
    }

    // --- Sprint Management steps ---

    @And("a sprint named {string} exists in project {string}")
    public void aSprintNamedExistsInProject(String sprintName, String projectName) {
        executeCommand(new String[]{"sprint", "add", sprintName, "--project", projectName}, null);
        Sprint loaded = DAOFactory.sprintDAO().loadByKey(sprintName);
        assertNotNull("Sprint should exist after creation", loaded);
    }

    @When("the user gets sprint {string} by its ID")
    public void theUserGetsSprintByItsID(String sprintName) {
        Sprint sprint = DAOFactory.sprintDAO().loadByKey(sprintName);
        assertNotNull("Sprint should exist to look up by ID", sprint);
        executeCommand(new String[]{"sprint", "get", String.valueOf(sprint.getId())}, null);
    }

    @When("the user runs detail with -s flag for sprint {string}")
    public void theUserRunsDetailWithSFlagForSprint(String sprintName) {
        Sprint sprint = DAOFactory.sprintDAO().loadByKey(sprintName);
        assertNotNull("Sprint should exist to look up by ID", sprint);
        executeCommand(new String[]{"detail", "-s", String.valueOf(sprint.getId())}, null);
    }

    @When("the user runs detail with -t flag for the created task")
    public void theUserRunsDetailWithTFlagForCreatedTask() {
        assertNotNull("Task should have been created", this.createdTaskId);
        executeCommand(new String[]{"detail", "-t", String.valueOf(this.createdTaskId)}, null);
    }

    @When("the user runs detail with -p flag for project {string}")
    public void theUserRunsDetailWithPFlagForProject(String projectName) {
        Project project = DAOFactory.projectDAO().loadByKey(projectName);
        assertNotNull("Project should exist to look up by ID", project);
        assertNotNull("Project should have an ID", project.getId());
        executeCommand(new String[]{"detail", "-p", String.valueOf(project.getId())}, null);
    }

    @When("the user gets project {string} by its ID")
    public void theUserGetsProjectByItsID(String projectName) {
        Project project = DAOFactory.projectDAO().loadByKey(projectName);
        assertNotNull("Project should exist to look up by ID", project);
        assertNotNull("Project should have an ID", project.getId());
        executeCommand(new String[]{"project", "get", String.valueOf(project.getId())}, null);
    }

    @Then("the system displays sprint created {string}")
    public void theSystemDisplaysSprintCreated(String name) {
        assertTrue("Expected sprint created message",
                consoleOutput.contains(name) && consoleOutput.contains("created"));
    }

    @Then("the system displays sprint details {string}")
    public void theSystemDisplaysSprintDetails(String name) {
        assertTrue("Expected sprint details", consoleOutput.contains(name));
    }

    @Then("the system displays sprint not found {string}")
    public void theSystemDisplaysSprintNotFound(String name) {
        assertTrue("Expected not found message", consoleOutput.contains("not found"));
    }

    @Then("the sprint {string} has start date {string}")
    public void theSprintHasStartDate(String name, String date) {
        Sprint loaded = DAOFactory.sprintDAO().loadByKey(name);
        assertNotNull("Sprint should exist", loaded);
        assertNotNull("Start date should be set", loaded.getStart_date());
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
        assertEquals(date, fmt.format(loaded.getStart_date()));
    }

    @Then("the sprint {string} is deleted")
    public void theSprintIsDeleted(String name) {
        Sprint loaded = DAOFactory.sprintDAO().loadByKey(name);
        assertNull("Sprint should be deleted", loaded);
        assertTrue("Expected deletion message", consoleOutput.contains("deleted"));
    }

    // --- Task Management steps ---

    @When("the user adds a task with title {string} to project {string} assigned to {string}")
    public void theUserAddsTaskToProject(String title, String projectName, String username) {
        Project project = DAOFactory.projectDAO().loadByKey(projectName);
        assertNotNull("Project should exist", project);
        executeCommand(new String[]{"task", "add", "--title", title, "--project", String.valueOf(project.getId()), "--assigned_to", username}, null);
    }

    @And("a task exists in project {string} assigned to {string}")
    public void aTaskExistsInProjectAssignedTo(String projectName, String username) {
        Project project = DAOFactory.projectDAO().loadByKey(projectName);
        assertNotNull("Project should exist", project);
        executeCommand(new String[]{"task", "add", "--title", "Test Task", "--project", String.valueOf(project.getId()), "--assigned_to", username}, null);
        assertTrue("Expected task creation output", consoleOutput.contains("created"));
        // Extract task ID from output like "Task 1234567890 created successfully."
        String[] words = consoleOutput.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("Task") && i + 1 < words.length) {
                try {
                    this.createdTaskId = Long.parseLong(words[i + 1]);
                    break;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        assertNotNull("Should have captured task ID", this.createdTaskId);
    }

    @Then("the system displays task created")
    public void theSystemDisplaysTaskCreated() {
        assertTrue("Expected task created message", consoleOutput.contains("created"));
    }

    @When("the user runs the command to get the created task")
    public void theUserRunsTheCommandToGetTheCreatedTask() {
        assertNotNull("Task ID must exist", this.createdTaskId);
        executeCommand(new String[]{"task", "get", String.valueOf(this.createdTaskId)}, null);
    }

    @Then("the system displays the task details")
    public void theSystemDisplaysTheTaskDetails() {
        assertTrue("Expected task details in output", consoleOutput.contains("Task:") || consoleOutput.contains("Title:"));
    }

    @When("the user runs the command to update the created task with {string}")
    public void theUserRunsTheCommandToUpdateTheCreatedTaskWith(String flags) {
        assertNotNull("Task ID must exist", this.createdTaskId);
        String[] flagTokens = flags.split("\\s+");
        String[] args = new String[2 + flagTokens.length];
        args[0] = "task";
        args[1] = "update";
        // Insert task ID after "update"
        String[] fullArgs = new String[3 + flagTokens.length];
        fullArgs[0] = "task";
        fullArgs[1] = "update";
        fullArgs[2] = String.valueOf(this.createdTaskId);
        System.arraycopy(flagTokens, 0, fullArgs, 3, flagTokens.length);
        executeCommand(fullArgs, null);
    }

    @Then("the created task has status {string}")
    public void theCreatedTaskHasStatus(String status) {
        Task loaded = DAOFactory.taskDAO().loadByKey(String.valueOf(this.createdTaskId));
        assertNotNull("Task should exist", loaded);
        assertEquals(status, loaded.getStatus().name());
    }

    @Then("the created task is assigned to {string}")
    public void theCreatedTaskIsAssignedTo(String username) {
        Task loaded = DAOFactory.taskDAO().loadByKey(String.valueOf(this.createdTaskId));
        assertNotNull("Task should exist", loaded);
        assertEquals(username, loaded.getAssigned_to());
    }

    @When("the user runs the command to delete the created task")
    public void theUserRunsTheCommandToDeleteTheCreatedTask() {
        assertNotNull("Task ID must exist", this.createdTaskId);
        // Defer like project delete — stdin needed from confirm step
        this.pendingCommand = "trak task delete " + this.createdTaskId;
    }

    @And("the user confirms the task deletion")
    public void theUserConfirmsTheTaskDeletion() {
        assertNotNull("Expected a pending delete command", this.pendingCommand);
        String[] tokens = this.pendingCommand.split(" ");
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
        executeCommand(args, "yes\n");
    }

    @Then("the created task is deleted")
    public void theCreatedTaskIsDeleted() {
        Task loaded = DAOFactory.taskDAO().loadByKey(String.valueOf(this.createdTaskId));
        assertNull("Task should be deleted", loaded);
        assertTrue("Expected deletion message", consoleOutput.contains("deleted"));
    }

    // --- Auth steps ---

    @Given("a user {string} exists with password {string}")
    public void aUserExistsWithPassword(String username, String password) {
        File storeDir = new File(TTApp.storedir);
        if (!storeDir.exists()) storeDir.mkdirs();
        UserService userService = new TrakUserService();
        userService.create(username, "Test", "User", username + "@example.com", password);
    }

    @Given("the user {string} is currently logged in")
    public void theUserIsCurrentlyLoggedIn(String username) {
        SessionDAO.save(new Session(username));
    }

    @Given("no user {string} exists")
    public void noUserExists(String username) {
        DAOFactory.userDAO().deleteByKey(username);
    }

    @Then("the user {string} is logged in")
    public void theUserIsLoggedIn(String username) {
        Session session = SessionDAO.load();
        assertNotNull("Session should exist", session);
        assertEquals(username, session.getLogged_in_user());
    }

    @Then("no user is logged in")
    public void noUserIsLoggedIn() {
        Session session = SessionDAO.load();
        assertNull("Session should not exist", session);
    }

    @Then("the output contains {string}")
    public void theOutputContains(String text) {
        assertTrue("Expected output to contain '" + text + "' but got: " + consoleOutput,
                consoleOutput.contains(text));
    }

    @When("the user signs up with username {string} password {string} first_name {string} last_name {string} email {string}")
    public void theUserSignsUp(String username, String password, String firstName, String lastName, String email) {
        File storeDir = new File(TTApp.storedir);
        if (!storeDir.exists()) storeDir.mkdirs();
        AuthService authService = new TrakAuthService();
        authService.signup(firstName, lastName, username, email, password);
    }

    // --- Password steps ---

    @Then("the user {string} has a password set")
    public void theUserHasAPasswordSet(String username) {
        User loaded = DAOFactory.userDAO().loadByKey(username);
        assertNotNull("User should exist", loaded);
        assertNotNull("Password hash should be set", loaded.getPassword_hash());
    }

    @Then("the user {string} has no password set")
    public void theUserHasNoPasswordSet(String username) {
        User loaded = DAOFactory.userDAO().loadByKey(username);
        assertNotNull("User should exist", loaded);
        assertNull("Password hash should be null", loaded.getPassword_hash());
    }

    @Then("the user {string} can authenticate with password {string}")
    public void theUserCanAuthenticate(String username, String password) {
        UserService userService = new TrakUserService();
        assertTrue("Should authenticate with correct password",
                userService.authenticate(username, password));
    }

    @Then("the user {string} cannot authenticate with password {string}")
    public void theUserCannotAuthenticate(String username, String password) {
        UserService userService = new TrakUserService();
        assertFalse("Should not authenticate with wrong password",
                userService.authenticate(username, password));
    }

    // --- Workspace steps ---

    @Given("the workspace user {string} is logged in")
    public void theWorkspaceUserIsLoggedIn(String username) {
        File storeDir = new File(TTApp.storedir);
        if (!storeDir.exists()) storeDir.mkdirs();
        // Create user with password and log in
        UserService userService = new TrakUserService();
        if (userService.getByUsername(username) == null) {
            userService.create(username, "Test", "User", username + "@example.com", "testpass");
        }
        Session s = new Session(username);
        SessionDAO.save(s);
        if (TTApp.getInstance() != null) {
            TTApp.getInstance().setSession(s);
        }
    }

    @Given("a project {string} exists with owner {string}")
    public void aProjectExistsWithOwner(String projectName, String ownerUsername) {
        File storeDir = new File(TTApp.storedir);
        if (!storeDir.exists()) storeDir.mkdirs();
        executeCommand(new String[]{"project", "add", projectName, "--owner", ownerUsername}, null);
    }

    @Given("a task assigned to {string} exists in project {string} with title {string}")
    public void aTaskAssignedToExistsInProjectWithTitle(String username, String projectName, String title) {
        Project project = DAOFactory.projectDAO().loadByKey(projectName);
        assertNotNull("Project should exist", project);
        executeCommand(new String[]{"task", "add", "--title", title, "--project", String.valueOf(project.getId()), "--assigned_to", username}, null);
        // Extract task ID from output
        String[] words = consoleOutput.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("Task") && i + 1 < words.length) {
                try {
                    this.createdTaskId = Long.parseLong(words[i + 1]);
                    break;
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    @And("the user starts the created task")
    public void theUserStartsTheCreatedTask() {
        assertNotNull("Task ID must exist", this.createdTaskId);
        executeCommand(new String[]{"start", String.valueOf(this.createdTaskId)}, null);
    }

    private String[] tokenize(String command) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inBrackets = false;
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if (c == '[') {
                inBrackets = true;
                current.append(c);
            } else if (c == ']') {
                inBrackets = false;
                current.append(c);
            } else if (c == ' ' && !inBrackets) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens.toArray(new String[0]);
    }

    private void executeCommand(String[] args, String input) {
        if (input != null) {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        System.setOut(printStream);
        System.setErr(printStream);

        try {
            Main.main(args);
        } catch (Exception e) {
            // Capture any error output
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
            System.setIn(originalIn);
        }

        consoleOutput = outputStream.toString().trim();
    }
}
