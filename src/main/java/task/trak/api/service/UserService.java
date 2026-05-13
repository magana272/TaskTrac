package task.trak.api.service;

import task.trak.api.dto.UserDTO;

public interface UserService {
    UserDTO create(String username, String firstName, String lastName, String email, String password);

    UserDTO getByUsername(String username);

    UserDTO getByEmail(String email);

    boolean deleteByUsername(String username);

    UserDTO updateByUsername(String username, String newFirstName, String newLastName, String newEmail, String newPassword);

    boolean authenticate(String username, String password);
}
