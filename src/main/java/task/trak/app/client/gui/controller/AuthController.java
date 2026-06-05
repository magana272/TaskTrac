package task.trak.app.client.gui.controller;

import task.trak.model.Session;
import task.trak.model.dto.request.UpdateUserRequest;
import task.trak.app.client.http.AuthHttpService;
import task.trak.app.client.http.UserHttpService;
import task.trak.app.client.gui.viewmodel.UserViewModel;

public class AuthController {

    private final AuthHttpService authService;
    private final UserHttpService userService;
    private final UserViewModel userViewModel;

    public AuthController(AuthHttpService authService, UserHttpService userService, UserViewModel userViewModel) {
        this.authService = authService;
        this.userService = userService;
        this.userViewModel = userViewModel;
    }

    public void login(String username, String password) {
        try {
            Session session = this.authService.login(username, password);
            userViewModel.setSession(session);
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }

    public void signup(String username, String firstName, String lastName, String email, String password) {
        try {
            Session session = this.authService.signup(firstName, lastName, username, email, password);
            userViewModel.setSession(session);
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }

    public void logout() {
        try {
            this.authService.logout();
            userViewModel.setSession(null);
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
        }
    }

    public boolean isLoggedIn() {
        try {
            return this.authService.isLoggedIn();
        } catch (Exception e) {
            userViewModel.setError(e.getMessage());
            return false;
        }
    }

    public void changePassword(String currentPassword, String newPassword) {
        Session session = userViewModel.getSession();
        if (session == null) throw new RuntimeException("Not logged in.");
        String username = session.getLogged_in_user();

        if (!userService.authenticate(username, currentPassword)) {
            throw new RuntimeException("Current password is incorrect.");
        }

        userService.updateByUsername(new UpdateUserRequest(username, null, null, null, newPassword));
    }

    public void deleteAccount(String password) {
        Session session = userViewModel.getSession();
        if (session == null) throw new RuntimeException("Not logged in.");
        String username = session.getLogged_in_user();

        if (!userService.authenticate(username, password)) {
            throw new RuntimeException("Password is incorrect.");
        }

        userService.deleteByUsername(username);
        try { authService.logout(); } catch (Exception ignored) { }
        userViewModel.setSession(null);
    }

    public Session getSession() {
        return userViewModel.getSession();
    }
}
