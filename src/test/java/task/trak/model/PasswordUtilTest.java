package task.trak.model;

import task.trak.app.server.util.PasswordUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class PasswordUtilTest {

    @Test
    public void TestHashProducesConsistentResult() {
        String hash1 = PasswordUtil.hash("mypassword");
        String hash2 = PasswordUtil.hash("mypassword");
        assertEquals(hash1, hash2);
    }

    @Test
    public void TestHashProduces64CharHex() {
        String hash = PasswordUtil.hash("test");
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }

    @Test
    public void TestDifferentPasswordsDifferentHashes() {
        String hash1 = PasswordUtil.hash("password1");
        String hash2 = PasswordUtil.hash("password2");
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void TestVerifyCorrectPassword() {
        String hash = PasswordUtil.hash("secret123");
        assertTrue(PasswordUtil.verify("secret123", hash));
    }

    @Test
    public void TestVerifyWrongPassword() {
        String hash = PasswordUtil.hash("secret123");
        assertFalse(PasswordUtil.verify("wrongpassword", hash));
    }

    @Test
    public void TestHashEmptyString() {
        String hash = PasswordUtil.hash("");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
}
