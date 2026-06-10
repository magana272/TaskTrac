package task.trak.model.dto.request;

import task.trak.model.exception.ValidationException;

public record CreateUserRequest(
        String username,
        String firstName,
        String lastName,
        String email,
        String password
) {
    public void validate() {
        if (username == null || username.isBlank()) throw new ValidationException("Username is required.");
        if (username.length() < 5 || username.length() > 17) {
            throw new ValidationException("Username must be 5–17 characters long.");
        }
        if (!username.matches("[a-zA-Z0-9]+")) {
            throw new ValidationException("Username must be alphanumeric.");
        }
        if (password != null) validatePassword(password);
    }

    public static void validatePassword(String password) {
        if (password.length() < 5 || password.length() > 16) {
            throw new ValidationException("Password must be 5–16 characters long.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("Password must contain a lowercase letter.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("Password must contain an uppercase letter.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new ValidationException("Password must contain a number.");
        }
        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            throw new ValidationException("Password must contain a symbol.");
        }
    }
}
