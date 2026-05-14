package task.trak.api.service;

import task.trak.api.dto.UserDTO;
import task.trak.api.dto.request.CreateUserRequest;
import task.trak.api.dto.request.UpdateUserRequest;

public interface UserService {
    UserDTO create(CreateUserRequest request);

    UserDTO getByUsername(String username);

    UserDTO getByEmail(String email);

    boolean deleteByUsername(String username);

    UserDTO updateByUsername(UpdateUserRequest request);

    boolean authenticate(String username, String password);
}
