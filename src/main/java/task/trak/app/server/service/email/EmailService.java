package task.trak.app.server.service.email;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
