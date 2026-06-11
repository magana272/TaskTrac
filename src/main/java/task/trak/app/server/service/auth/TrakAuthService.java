package task.trak.app.server.service.auth;

import task.trak.model.dto.UserDTO;
import task.trak.model.dto.request.CreateUserRequest;
import task.trak.model.Session;
import task.trak.api.service.AuthService;
import task.trak.api.service.UserService;
import task.trak.app.server.dao.SessionDAO;

public class TrakAuthService implements AuthService {

    private final UserService userService;

    public TrakAuthService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Session login(String username, String password) {
        if (!userService.authenticate(username, password)) {
            return null;
        }
        Session session = new Session(username);
        SessionDAO.save(session);
        return session;
    }

    @Override
    public Session signup(String firstName, String lastName, String username, String email, String password) {
        UserDTO existing = userService.getByUsername(username);
        if (existing != null) {
            throw new IllegalArgumentException("User \"" + username + "\" already exists.");
        }
        if (email != null) {
            UserDTO byEmail = userService.getByEmail(email);
            if (byEmail != null) {
                throw new IllegalArgumentException("Email \"" + email + "\" is already in use.");
            }
        }
        userService.create(new CreateUserRequest(username, firstName, lastName, email, password));
        Session session = new Session(username);
        SessionDAO.save(session);
        return session;
    }

    @Override
    public void logout() {
        SessionDAO.clear();
    }

    @Override
    public Session loginWithGoogle(String idToken) {
        throw new UnsupportedOperationException("Google login is not supported in local mode.");
    }

    @Override
    public String requestPasswordReset(String email) {
        throw new UnsupportedOperationException("Password reset is not supported in local mode.");
    }

    @Override
    public String resetPassword(String code, String newPassword) {
        throw new UnsupportedOperationException("Password reset is not supported in local mode.");
    }
}
