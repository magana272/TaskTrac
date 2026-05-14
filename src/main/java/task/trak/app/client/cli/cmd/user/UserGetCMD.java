package task.trak.app.client.cli.cmd.user;

import task.trak.model.dto.UserDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.UserService;

import java.util.Optional;

public class UserGetCMD extends UserCMD {
    private final UserService userService = ServiceFactory.userService();
    private final String username;

    public UserGetCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Username is required for get command");
        }
        this.username = args[0];
    }

    @Override
    public Optional<UserDTO> Execute() throws Exception {
        UserDTO user = userService.getByUsername(this.username);
        if (user != null) {
            System.out.println("User: " + user.userName());
            System.out.println("Name: " + user.firstName() + " " + user.lastName());
            if (user.email() != null) {
                System.out.println("Email: " + user.email());
            }
            return Optional.of(user);
        } else {
            System.out.println("User \"" + this.username + "\" not found.");
            return Optional.empty();
        }
    }

    @Override
    public void accept(String[] strings) {
        try {
            this.Execute();
        } catch (Exception e) {
            System.err.println("Error getting user: " + e.getMessage());
        }
    }
}
