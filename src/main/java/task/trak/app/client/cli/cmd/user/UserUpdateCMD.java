package task.trak.app.client.cli.cmd.user;

import task.trak.api.dto.UserDTO;
import task.trak.api.dto.request.UpdateUserRequest;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.UserService;

import java.util.HashMap;
import java.util.Optional;

public class UserUpdateCMD extends UserCMD {
    private final UserService userService = ServiceFactory.userService();
    private String username;
    private HashMap<String, String> options;

    public UserUpdateCMD(String[] args) {
        super();
        this.parse(args);
    }

    public HashMap<String, String> parse(String[] args) {
        if (args == null || args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: user update <username> --<field> <value>");
        }
        this.username = args[0];
        HashMap<String, String> parsed = new HashMap<>();
        int i = 1;
        while (i < args.length) {
            if (args[i].startsWith("--")) {
                String key = args[i].replaceFirst("^--", "");
                StringBuilder value = new StringBuilder();
                i++;
                while (i < args.length && !args[i].startsWith("--")) {
                    if (value.length() > 0) value.append(" ");
                    value.append(args[i]);
                    i++;
                }
                parsed.put(key, value.toString());
            } else {
                i++;
            }
        }
        this.options = parsed;
        return parsed;
    }

    @Override
    public Optional<UserDTO> Execute() throws Exception {
        String newFirstName = this.options.get("first_name");
        String newLastName = this.options.get("last_name");
        String newEmail = this.options.get("email");
        String newPassword = this.options.get("password");

        UserDTO updated = userService.updateByUsername(new UpdateUserRequest(this.username, newFirstName, newLastName, newEmail, newPassword));
        System.out.println("User \"" + this.username + "\" updated successfully.");
        return Optional.of(updated);
    }

    @Override
    public void accept(String[] strings) {
        try {
            this.Execute();
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }
}
