package club.sqlhub.utils.Auth;

import java.util.Random;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import club.sqlhub.constants.AppConstants;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OtpHandler {

    private final Random otp = new Random();
    private final JavaMailSender mailSender;

    public String generateOTP() {
        return String.format("%06d", otp.nextInt(999999));
    }

    public String otpKey(String email) {
        return AppConstants.REDIS_OTP_KEY + email;
    }

    public String limitKey(String email) {
        return AppConstants.REDIS_OTP_RATE_LIMIT__KEY + email;
    }

    public String emailVerificationKey(String uuid) {
        return AppConstants.REDIS_EMAIL_VERIFICATION_KEY + uuid;
    }

    public String generateUuidForEmailVerification(String email) {
        return UUID.randomUUID().toString() + "-" + email;
    }

    public void sendEmailOTP(String toEmail, String otp) {

        try {
            MimeMessage minmeMessage = mailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(minmeMessage, "utf-8");
            message.setTo(toEmail);
            message.setSubject("Your OTP Code");
            message.setText(OTPTemplate.getOtpHtmlTemplate(otp), true);

            mailSender.send(minmeMessage);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send OTP email");
        }
    }
}
