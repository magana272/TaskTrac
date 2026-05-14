package task.trak.app.client.cli.cmd.workspace;

import task.trak.model.Session;
import task.trak.api.service.AuthService;
import task.trak.api.service.ServiceFactory;
import task.trak.app.client.cli.TTApp;
import task.trak.app.client.cli.cmd.cmdtype.CMD;

import java.util.HashMap;
import java.util.Optional;

public class LoginCMD implements CMD<Void> {
    private final AuthService authService = ServiceFactory.authService();
    private String username;
    private HashMap<String, String> options;

    public LoginCMD(String[] args) {
        this.parse(args);
    }

    private void parse(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Usage: login <username> --password <password>");
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
        String password = this.options.get("password");
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("--password is required. Usage: login <username> --password <password>");
        }

        Session session = authService.login(this.username, password);
        if (session != null) {
            if (TTApp.getInstance() != null) {
                TTApp.getInstance().setSession(session);
            }
            System.out.println("Logged in as " + this.username + ".");
        } else {
            System.out.println("Invalid username or password.");
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
