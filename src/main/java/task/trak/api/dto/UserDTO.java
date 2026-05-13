package task.trak.api.dto;

public record UserDTO(
        Long id,
        String firstName,
        String lastName,
        String userName,
        String email
) {
}
