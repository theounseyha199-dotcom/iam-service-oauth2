package kh.edu.iam.infrastructure.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final ObjectProvider<JavaMailSender> mail;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> mail,
            @Value("${app.iam.mail-from:no-reply@localhost}") String from) {
        this.mail = mail;
        this.from = from;
    }

    public void sendVerificationCode(String email, String code) {
        var sender = mail.getIfAvailable();
        if (sender == null) throw new IllegalStateException("Mail sender unavailable");
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Your ISTAD Identity verification code");
        message.setText("Your verification code is: " + code
                + "\n\nEnter this code on the ISTAD Identity verification page. It expires in 5 minutes and works once."
                + "\nNever share this code. If you did not request it, ignore this email.");
        sender.send(message);
    }
}
