package club.sqlhub.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EmailConfig {

    private final Dotenv dotenv;

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(dotenv.get("SMTP_HOST"));
        sender.setPort(Integer.parseInt(dotenv.get("SMTP_PORT")));
        sender.setUsername(dotenv.get("SMTP_USERNAME"));
        sender.setPassword(dotenv.get("SMTP_PASSWORD"));

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return sender;
    }

}
