package task.trak.app.server.server;

import com.sun.net.httpserver.HttpHandler;

/**
 * Authentication filter that wraps HttpHandlers with bearer token enforcement.
 */
public class AuthFilter {

    /**
     * Wraps a handler to require a valid bearer token.
     * Returns 401 if the token is missing or invalid.
     * Stores the authenticated username in the exchange attributes.
     */
    public static HttpHandler requireAuth(HttpHandler handler) {
        return exchange -> {
            String token = SessionManager.extractToken(exchange);
            if (token == null || SessionManager.getUsername(token) == null) {
                JsonHelper.sendError(exchange, 401, "Authentication required.");
                return;
            }
            exchange.setAttribute("username", SessionManager.getUsername(token));
            handler.handle(exchange);
        };
    }
}
