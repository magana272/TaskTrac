package task.trak.app.client.cli.cmd.user;

import task.trak.model.dto.UserDTO;
import task.trak.model.dto.request.CreateUserRequest;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.UserService;

import java.util.HashMap;
import java.util.Optional;

public class UserAddCMD extends UserCMD {
    private final UserService userService = ServiceFactory.userService();
    private final String username;
    private HashMap<String, String> options;

    public UserAddCMD(String[] args) {
        super();
        this.username = args[0];
        this.parse(args);
    }

    public HashMap<String, String> parse(String[] args) {
        HashMap<String, String> parsed = new HashMap<>();
        parsed.put("username", args[0]);
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
        String firstName = this.options.get("first_name");
        String lastName = this.options.get("last_name");
        String email = this.options.get("email");
        String password = this.options.get("password");

        UserDTO user = userService.create(new CreateUserRequest(this.username, firstName, lastName, email, password));
        System.out.println("User \"" + this.username + "\" created successfully.");
        return Optional.of(user);
    }

    @Override
    public void accept(String[] strings) {
        try {
            this.Execute();
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
    }
}
