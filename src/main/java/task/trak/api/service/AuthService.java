package task.trak.api.service;

import task.trak.model.Session;

public interface AuthService {
    Session login(String username, String password);

    Session signup(String firstName, String lastName, String username, String email, String password);

    void logout();

    Session loginWithGoogle(String idToken);

    String requestPasswordReset(String email);

    String resetPassword(String code, String newPassword);
}
