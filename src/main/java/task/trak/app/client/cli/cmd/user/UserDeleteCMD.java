package task.trak.app.client.cli.cmd.user;

import task.trak.model.dto.UserDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.UserService;

import java.util.Optional;
import java.util.Scanner;

public class UserDeleteCMD extends UserCMD {
    private final UserService userService = ServiceFactory.userService();
    private final String username;

    public UserDeleteCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Username is required for delete command");
        }
        this.username = args[0];
    }

    @Override
    public Optional<UserDTO> Execute() throws Exception {
        System.out.println("Confirm deletion of user \"" + this.username + "\"? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes")) {
            boolean deleted = userService.deleteByUsername(this.username);
            if (deleted) {
                System.out.println("User \"" + this.username + "\" deleted successfully.");
            } else {
                System.out.println("User \"" + this.username + "\" not found.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
        return Optional.empty();
    }

    @Override
    public void accept(String[] strings) {
        try {
            this.Execute();
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }
}
