package task.trak.app.server.server;

import com.sun.net.httpserver.HttpExchange;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final Map<String, String> tokenToUser = new ConcurrentHashMap<>();

    public static String createToken(String username) {
        String token = UUID.randomUUID().toString();
        tokenToUser.put(token, username);
        return token;
    }

    public static String getUsername(String token) {
        if (token == null) return null;
        return tokenToUser.get(token);
    }

    public static void removeToken(String token) {
        if (token != null) {
            tokenToUser.remove(token);
        }
    }

    public static String extractToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }
}
