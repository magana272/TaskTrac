package task.trak.app.client.gui.controller;

import task.trak.api.model.Session;
import task.trak.api.service.ServiceFactory;
import task.trak.app.client.gui.viewmodel.UserViewModel;

public class AuthController {

    private final UserViewModel userViewModel;

    public AuthController(UserViewModel userViewModel) {
        this.userViewModel = userViewModel;
    }

    public void login(String username, String password) {
        Session session = ServiceFactory.authService().login(username, password);
        userViewModel.setSession(session);
    }

    public void signup(String username, String firstName, String lastName, String email, String password) {
        Session session = ServiceFactory.authService().signup(firstName, lastName, username, email, password);
        userViewModel.setSession(session);
    }

    public void logout() {
        ServiceFactory.authService().logout();
        userViewModel.setSession(null);
    }

    public boolean isLoggedIn() {
        return ServiceFactory.authService().isLoggedIn();
    }

    public Session getSession() {
        return userViewModel.getSession();
    }
}
