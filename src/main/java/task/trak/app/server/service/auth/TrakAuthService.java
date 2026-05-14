package task.trak.app.server.service.auth;

import task.trak.api.dto.UserDTO;
import task.trak.api.dto.request.CreateUserRequest;
import task.trak.api.model.Session;
import task.trak.api.service.AuthService;
import task.trak.api.service.UserService;
import task.trak.app.server.dao.SessionDAO;
import task.trak.app.server.service.user.TrakUserService;

public class TrakAuthService implements AuthService {

    private final UserService userService = new TrakUserService();

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
    public Session getCurrentSession() {
        return SessionDAO.load();
    }

    @Override
    public boolean isLoggedIn() {
        Session session = getCurrentSession();
        return session != null && session.getLogged_in_user() != null;
    }
}
