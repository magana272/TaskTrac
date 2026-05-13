package task.trak.app.client.cli.cmd.workspace;

import task.trak.api.model.Session;
import task.trak.api.service.AuthService;
import task.trak.api.service.ServiceFactory;
import task.trak.app.client.cli.TTApp;
import task.trak.app.client.cli.cmd.cmdtype.CMD;

import java.util.HashMap;
import java.util.Optional;

public class SignupCMD implements CMD<Void> {
    private final AuthService authService = ServiceFactory.authService();
    private String username;
    private HashMap<String, String> options;

    public SignupCMD(String[] args) {
        this.parse(args);
    }

    private void parse(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException(
                    "Usage: signup <username> --first_name <first> --last_name <last> --email <email> --password <password>");
        }
        this.username = args[0];
        this.options = new HashMap<>();
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
                options.put(key, value.toString());
            } else {
                i++;
            }
        }
    }

    @Override
    public Optional<Void> Execute() throws Exception {
        String firstName = this.options.get("first_name");
        String lastName = this.options.get("last_name");
        String email = this.options.get("email");
        String password = this.options.get("password");

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("--password is required.");
        }

        try {
            Session session = authService.signup(firstName, lastName, this.username, email, password);
            if (session != null) {
                if (TTApp.getInstance() != null) {
                    TTApp.getInstance().setSession(session);
                }
                System.out.println("Account created. Logged in as " + this.username + ".");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void accept(String[] strings) {
        try {
            this.Execute();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
