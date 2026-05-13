import org.junit.Test;

import static org.junit.Assert.*;

public class HttpServicePackageTest {

    @Test
    public void testApiClientPackage() throws ClassNotFoundException {
        Class<?> cls = Class.forName("task.trak.app.client.http.ApiClient");
        assertEquals("task.trak.app.client.http", cls.getPackageName());
    }

    @Test
    public void testTaskHttpServicePackage() throws ClassNotFoundException {
        Class<?> cls = Class.forName("task.trak.app.client.http.TaskHttpService");
        assertEquals("task.trak.app.client.http", cls.getPackageName());
    }

    @Test
    public void testProjectHttpServicePackage() throws ClassNotFoundException {
        Class<?> cls = Class.forName("task.trak.app.client.http.ProjectHttpService");
        assertEquals("task.trak.app.client.http", cls.getPackageName());
    }

    @Test
    public void testUserHttpServicePackage() throws ClassNotFoundException {
        Class<?> cls = Class.forName("task.trak.app.client.http.UserHttpService");
        assertEquals("task.trak.app.client.http", cls.getPackageName());
    }

    @Test
    public void testAuthHttpServicePackage() throws ClassNotFoundException {
        Class<?> cls = Class.forName("task.trak.app.client.http.AuthHttpService");
        assertEquals("task.trak.app.client.http", cls.getPackageName());
    }

    @Test
    public void testSprintHttpServicePackage() throws ClassNotFoundException {
        Class<?> cls = Class.forName("task.trak.app.client.http.SprintHttpService");
        assertEquals("task.trak.app.client.http", cls.getPackageName());
    }

    @Test
    public void testBacklogHttpServicePackage() throws ClassNotFoundException {
        Class<?> cls = Class.forName("task.trak.app.client.http.BacklogHttpService");
        assertEquals("task.trak.app.client.http", cls.getPackageName());
    }
}
