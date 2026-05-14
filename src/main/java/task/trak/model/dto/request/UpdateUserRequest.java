package task.trak.model.dto.request;

import task.trak.model.exception.ValidationException;

public record UpdateUserRequest(
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
