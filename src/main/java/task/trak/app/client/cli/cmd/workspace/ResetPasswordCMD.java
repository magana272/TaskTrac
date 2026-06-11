package task.trak.app.client.cli.cmd.workspace;

import task.trak.app.client.cli.cmd.cmdtype.CMD;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.UserService;
import task.trak.model.dto.request.CreateUserRequest;
import task.trak.model.dto.request.UpdateUserRequest;
import task.trak.app.server.server.PasswordResetStore;

import java.util.Optional;

public class ResetPasswordCMD implements CMD<String> {

    private String code;
    private String password;

    public ResetPasswordCMD(String[] args) {
        parse(args);
    }

    private void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--code".equals(args[i]) && i + 1 < args.length) {
                this.code = args[++i];
            } else if ("--password".equals(args[i]) && i + 1 < args.length) {
                this.password = args[++i];
            }
        }
    }

    @Override
    public Optional<String> Execute() throws Exception {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Usage: reset-password --code <code> --password <newpassword>");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Usage: reset-password --code <code> --password <newpassword>");
        }

        String username = PasswordResetStore.validateCode(code);
        if (username == null) {
            throw new IllegalArgumentException("Invalid or expired reset code");
        }

        CreateUserRequest.validatePassword(password);

        UserService userService = ServiceFactory.userService();
        userService.updateByUsername(new UpdateUserRequest(username, null, null, null, password));

        PasswordResetStore.removeCode(code);

        System.out.println("Password reset successful.");
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
