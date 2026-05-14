package task.trak.api.dto.request;

import task.trak.api.exception.ValidationException;

public record CreateUserRequest(
        String username,
        String firstName,
        String lastName,
        String email,
        String password
) {
    public void validate() {
        if (username == null || username.isBlank()) throw new ValidationException("Username is required.");
    }
}
