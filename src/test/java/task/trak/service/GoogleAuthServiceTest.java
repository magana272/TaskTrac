package task.trak.service;

import org.junit.Before;
import org.junit.Test;
import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.service.auth.GoogleAuthService;
import task.trak.app.server.service.user.TrakUserService;
import task.trak.api.service.UserService;
import task.trak.model.dto.UserDTO;
import task.trak.model.dto.request.CreateUserRequest;

import java.io.File;

import static org.junit.Assert.*;

public class GoogleAuthServiceTest {

    private UserService userService;

    @Before
    public void setUp() {
        TTApp.storedir = "src/test/.store";
        File storeDir = new File(TTApp.storedir);
        if (!storeDir.exists()) storeDir.mkdirs();
        DAOFactory.setFormat(DAOFactory.Format.JSON);
        userService = new TrakUserService();

        // Clean up test users
        if (userService.getByUsername("googletest") != null) {
            userService.deleteByUsername("googletest");
        }
    }

    @Test
    public void testVerifyAndLoginRejectsNullToken() {
        GoogleAuthService service = new GoogleAuthService(userService);
        try {
            service.verifyAndLogin(null);
            fail("Should reject null token");
        } catch (IllegalArgumentException e) {
            assertEquals("ID token is required.", e.getMessage());
        }
    }

    @Test
    public void testVerifyAndLoginRejectsBlankToken() {
        GoogleAuthService service = new GoogleAuthService(userService);
        try {
            service.verifyAndLogin("   ");
            fail("Should reject blank token");
        } catch (IllegalArgumentException e) {
            assertEquals("ID token is required.", e.getMessage());
        }
    }

    @Test
    public void testVerifyAndLoginRejectsInvalidToken() {
        GoogleAuthService service = new GoogleAuthService(userService);
        try {
            service.verifyAndLogin("totally-invalid-token");
            fail("Should reject invalid token");
        } catch (RuntimeException e) {
            // Expected: either "Invalid Google token." or network error
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testOAuthUserCreatedWithoutPassword() {
        // Create a user without password (simulating OAuth user creation)
        userService.create(new CreateUserRequest("oauthuser1", "OAuth", "User", "oauth@test.com", null));
        UserDTO user = userService.getByUsername("oauthuser1");
        assertNotNull(user);
        assertEquals("oauth@test.com", user.email());
        // OAuth users should not be able to authenticate with any password
        assertFalse(userService.authenticate("oauthuser1", "anything"));

        // Cleanup
        userService.deleteByUsername("oauthuser1");
    }

    @Test
    public void testGetByEmailFindsExistingUser() {
        userService.create(new CreateUserRequest("googletest", "Google", "Test", "gtest@gmail.com", "Pass1!"));
        UserDTO found = userService.getByEmail("gtest@gmail.com");
        assertNotNull(found);
        assertEquals("googletest", found.userName());

        // Cleanup
        userService.deleteByUsername("googletest");
    }

    @Test
    public void testGetByEmailReturnsNullForUnknown() {
        UserDTO found = userService.getByEmail("nonexistent@gmail.com");
        assertNull(found);
    }
}
