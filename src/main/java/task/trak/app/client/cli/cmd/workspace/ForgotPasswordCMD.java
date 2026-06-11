package task.trak.app.client.cli.cmd.workspace;

import task.trak.app.client.cli.cmd.cmdtype.CMD;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.UserService;
import task.trak.model.dto.UserDTO;
import task.trak.app.server.server.PasswordResetStore;
import task.trak.app.server.service.email.SmtpEmailService;
import task.trak.app.server.service.email.EmailService;

import java.util.Optional;

public class ForgotPasswordCMD implements CMD<String> {

    private String email;

    public ForgotPasswordCMD(String[] args) {
        parse(args);
    }

    private void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--email".equals(args[i]) && i + 1 < args.length) {
                this.email = args[++i];
            }
        }
    }

    @Override
    public Optional<String> Execute() throws Exception {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Usage: forgot-password --email <email>");
        }

        UserService userService = ServiceFactory.userService();
        UserDTO user = userService.getByEmail(email);

        if (user != null) {
            String code = PasswordResetStore.createCode(user.userName());
            try {
                EmailService emailService = new SmtpEmailService();
                emailService.sendEmail(email, "Trak Password Reset",
                        "Your password reset code is: " + code + "\n\nThis code expires in 15 minutes.");
            } catch (Exception e) {
                System.err.println("Failed to send email: " + e.getMessage());
            }
        }

        System.out.println("If an account with that email exists, a reset code has been sent.");
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
