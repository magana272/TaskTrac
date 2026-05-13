package task.trak.app.client.cli.cmd.workspace;

import task.trak.api.service.AuthService;
import task.trak.api.service.ServiceFactory;
import task.trak.app.client.cli.TTApp;
import task.trak.app.client.cli.cmd.cmdtype.CMD;

import java.util.Optional;

public class LogoutCMD implements CMD<Void> {
    private final AuthService authService = ServiceFactory.authService();

    public LogoutCMD(String[] args) {
    }

    @Override
    public Optional<Void> Execute() throws Exception {
        authService.logout();
        if (TTApp.getInstance() != null) {
            TTApp.getInstance().setSession(null);
        }
        System.out.println("Logged out.");
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
