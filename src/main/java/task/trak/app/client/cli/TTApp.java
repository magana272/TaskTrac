package task.trak.app.client.cli;

import task.trak.model.Session;
import task.trak.api.service.AuthService;
import task.trak.api.service.ServiceFactory;
import task.trak.app.App;
import task.trak.app.client.cli.cmd.CMD_Factory;

import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TTApp implements App {
    public static String storedir = ".store";
    private static App instance;

    private Session session;
    private Consumer<Session> sessionSaver;
    private Supplier<Session> sessionLoader;

    public TTApp(String[] args) {
        instance = this;

        if (args == null || args.length == 0) {
            handleNoArgs();
        } else {
            try {
                CMD_Factory cmdFactory = new CMD_Factory(args);
                cmdFactory.accept(args);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private TTApp() {
        // Private no-arg for createLocal
    }

    public TTApp(CMD_Factory cmdFactory, String[] args) {
        instance = this;
        cmdFactory.accept(args);
    }

    /**
     * Create TTApp with local session persistence — loads session BEFORE dispatching commands.
     */
    public static TTApp createLocal(String[] args, Supplier<Session> loader, Consumer<Session> saver) {
        TTApp app = new TTApp();
        app.sessionLoader = loader;
        app.sessionSaver = saver;
        app.session = loader.get();
        instance = app;

        if (args == null || args.length == 0) {
            app.handleNoArgs();
        } else {
            try {
                CMD_Factory cmdFactory = new CMD_Factory(args);
                cmdFactory.accept(args);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        return app;
    }

    public static App getInstance() {
        return instance;
    }

    public static void setInstance(App app) {
        instance = app;
    }

    /**
     * Set the persistence callbacks for session management.
     * Must be called before operations that need session loading/saving.
     */
    public void setSessionPersistence(Supplier<Session> loader, Consumer<Session> saver) {
        this.sessionLoader = loader;
        this.sessionSaver = saver;
    }

    /**
     * Initialize session from the loader if available.
     */
    public void initSession() {
        if (this.sessionLoader != null && this.session == null) {
            this.session = this.sessionLoader.get();
        }
    }

    @Override
    public Session getSession() {
        if (this.session == null && this.sessionLoader != null) {
            this.session = this.sessionLoader.get();
        }
        return this.session;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    private void handleNoArgs() {
        if (this.session != null && this.session.getLogged_in_user() != null) {
            System.out.println("Logged in as: " + this.session.getLogged_in_user());
            System.out.println("\nAvailable commands: projects, tasks, detail, cur, start, end, info, logout");
        } else {
            System.out.println("Welcome to Trak!");
            System.out.println("1. Login");
            System.out.println("2. Create Account");
            System.out.print("Choose (1/2): ");
            Scanner scanner = new Scanner(System.in);
            String choice = scanner.nextLine().trim();
            AuthService authService = ServiceFactory.authService();
            if ("1".equals(choice)) {
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Password: ");
                String password = scanner.nextLine().trim();
                Session s = authService.login(username, password);
                if (s != null) {
                    this.session = s;
                    System.out.println("Logged in as " + username + ".");
                } else {
                    System.out.println("Invalid username or password.");
                }
            } else if ("2".equals(choice)) {
                System.out.print("First name: ");
                String firstName = scanner.nextLine().trim();
                System.out.print("Last name: ");
                String lastName = scanner.nextLine().trim();
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Email: ");
                String email = scanner.nextLine().trim();
                System.out.print("Password: ");
                String password = scanner.nextLine().trim();
                try {
                    this.session = authService.signup(firstName, lastName, username, email, password);
                    System.out.println("Account created. Logged in as " + username + ".");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void save() {
        if (this.session != null && this.sessionSaver != null) {
            this.sessionSaver.accept(this.session);
        }
    }
}
