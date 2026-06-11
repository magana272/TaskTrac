package task.trak.app.server.service.email;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import task.trak.app.client.config.EnvLoader;

import java.util.Properties;

public class SmtpEmailService implements EmailService {

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String from;

    public SmtpEmailService() {
        this.host = env("TRAK_SMTP_HOST", "smtp.gmail.com");
        this.port = Integer.parseInt(env("TRAK_SMTP_PORT", "587"));
        this.user = env("TRAK_SMTP_USER", "");
        this.password = env("TRAK_SMTP_PASSWORD", "");
        this.from = env("TRAK_SMTP_FROM", this.user);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    private static String env(String key, String defaultValue) {
        return EnvLoader.get(key, defaultValue);
    }
}
