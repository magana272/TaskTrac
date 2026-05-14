package task.trak.app.server.service.user;

import task.trak.model.dto.UserDTO;
import task.trak.model.dto.request.CreateUserRequest;
import task.trak.model.dto.request.UpdateUserRequest;
import task.trak.model.exception.EntityNotFoundException;
import task.trak.api.service.UserService;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.user.TrakBuilderUser;
import task.trak.app.server.model.user.User;
import task.trak.app.server.util.PasswordUtil;

public class TrakUserService implements UserService {

    private final EntityDAO<User> store = DAOFactory.userDAO();

    @Override
    public UserDTO create(CreateUserRequest request) {
        request.validate();
        TrakBuilderUser builder = new TrakBuilderUser();
        builder.setUserName(request.username());
        if (request.firstName() != null) builder.setFirstName(request.firstName());
        if (request.lastName() != null) builder.setLastName(request.lastName());
        if (request.email() != null) builder.setEmail(request.email());
        if (request.password() != null) builder.setPassword(request.password());
        User user = builder.build();
        store.save(user);
        return toDTO(user);
    }

    @Override
    public UserDTO getByUsername(String username) {
        User user = store.loadByKey(username);
        return user != null ? toDTO(user) : null;
    }

    @Override
    public UserDTO getByEmail(String email) {
        if (email == null) return null;
        return store.loadAll().stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst()
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    public boolean deleteByUsername(String username) {
        return store.deleteByKey(username);
    }

    @Override
    public UserDTO updateByUsername(UpdateUserRequest request) {
        request.validate();
        User user = store.loadByKey(request.username());
        if (user == null) {
            throw new EntityNotFoundException("User \"" + request.username() + "\" not found.");
        }

        if (request.firstName() != null) {
            user.setFirst_name(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLast_name(request.lastName());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.password() != null) {
            user.setPassword_hash(PasswordUtil.hash(request.password()));
        }

        store.save(user);
        return toDTO(user);
    }

    @Override
    public boolean authenticate(String username, String password) {
        User user = store.loadByKey(username);
        if (user == null || user.getPassword_hash() == null) {
            return false;
        }
        return PasswordUtil.verify(password, user.getPassword_hash());
    }

    private UserDTO toDTO(User u) {
        return new UserDTO(u.getID(), u.getFirst_name(), u.getLast_name(), u.getUser_name(), u.getEmail());
    }
}
