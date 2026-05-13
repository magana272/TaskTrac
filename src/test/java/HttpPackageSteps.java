import io.cucumber.java.en.*;
import task.trak.api.service.ServiceFactory;

import static org.junit.Assert.*;

public class HttpPackageSteps {

    @Then("the class {string} exists")
    public void theClassExists(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            fail("Class not found: " + className);
        }
    }

    @Given("HTTP services are registered")
    public void httpServicesAreRegistered() {
        ServiceFactory.registerHttpServices();
    }

    @When("the task service is requested from ServiceFactory")
    public void theTaskServiceIsRequested() {
        // ServiceFactory.taskService() is called in the Then step
    }

    @Then("the returned service class name contains {string}")
    public void theReturnedServiceClassNameContains(String expected) {
        String className = ServiceFactory.taskService().getClass().getSimpleName();
        assertTrue("Expected class name to contain '" + expected + "' but got '" + className + "'",
                className.contains(expected));
    }
}
