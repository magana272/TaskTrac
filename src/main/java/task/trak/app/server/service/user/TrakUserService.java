package task.trak.app.server.service.user;

import task.trak.api.dto.UserDTO;
import task.trak.api.service.UserService;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.user.TrakBuilderUser;
import task.trak.app.server.model.user.User;
import task.trak.app.server.util.PasswordUtil;

public class TrakUserService implements UserService {

    private final EntityDAO<User> store = DAOFactory.userDAO();

    @Override
    public UserDTO create(String username, String firstName, String lastName, String email, String password) {
        TrakBuilderUser builder = new TrakBuilderUser();
        builder.setUserName(username);
        if (firstName != null) builder.setFirstName(firstName);
        if (lastName != null) builder.setLastName(lastName);
        if (email != null) builder.setEmail(email);
        if (password != null) builder.setPassword(password);
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
    public UserDTO updateByUsername(String username, String newFirstName, String newLastName, String newEmail, String newPassword) {
        User user = store.loadByKey(username);
        if (user == null) {
            throw new IllegalArgumentException("User \"" + username + "\" not found.");
        }

        if (newFirstName != null) {
            user.setFirst_name(newFirstName);
        }
        if (newLastName != null) {
            user.setLast_name(newLastName);
        }
        if (newEmail != null) {
            user.setEmail(newEmail);
        }
        if (newPassword != null) {
            user.setPassword_hash(PasswordUtil.hash(newPassword));
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
