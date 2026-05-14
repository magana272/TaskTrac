package task.trak.app.client.gui.controller;

import task.trak.model.Session;
import task.trak.app.client.http.AuthHttpService;
import task.trak.app.client.gui.viewmodel.UserViewModel;

public class AuthController {

    private final AuthHttpService authService;
    private final UserViewModel userViewModel;

    public AuthController(AuthHttpService authService, UserViewModel userViewModel) {
        this.authService = authService;
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

    public Session getSession() {
        return userViewModel.getSession();
    }
}
