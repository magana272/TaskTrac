package task.trak.app.server.server;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PasswordResetStore {

    private record ResetEntry(String username, long expiresAt) {}

    private static final Map<String, ResetEntry> codeToEntry = new ConcurrentHashMap<>();
    private static final long EXPIRY_MS = 15 * 60 * 1000; // 15 minutes
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String createCode(String username) {
        cleanExpired();
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        codeToEntry.put(code, new ResetEntry(username, System.currentTimeMillis() + EXPIRY_MS));
        return code;
    }

    public static String validateCode(String code) {
        cleanExpired();
        ResetEntry entry = codeToEntry.get(code);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expiresAt()) {
            codeToEntry.remove(code);
            return null;
        }
        return entry.username();
    }

    public static void removeCode(String code) {
        codeToEntry.remove(code);
    }

    public static void clear() {
        codeToEntry.clear();
    }

    private static void cleanExpired() {
        long now = System.currentTimeMillis();
        codeToEntry.entrySet().removeIf(e -> now > e.getValue().expiresAt());
    }
}
