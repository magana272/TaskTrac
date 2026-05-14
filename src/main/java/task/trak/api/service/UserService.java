package task.trak.api.service;

import task.trak.model.dto.UserDTO;
import task.trak.model.dto.request.CreateUserRequest;
import task.trak.model.dto.request.UpdateUserRequest;

public interface UserService {
    UserDTO create(CreateUserRequest request);

    UserDTO getByUsername(String username);

    UserDTO getByEmail(String email);

    boolean deleteByUsername(String username);

    UserDTO updateByUsername(UpdateUserRequest request);

    boolean authenticate(String username, String password);
}
