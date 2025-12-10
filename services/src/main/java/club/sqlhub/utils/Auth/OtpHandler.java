package club.sqlhub.utils.Auth;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import club.sqlhub.constants.AppConstants;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OtpHandler {

    private final Random otp = new Random();

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

    public String passwordResetKey(String email) {
        return AppConstants.PASSWORD_RESET_KEY + email;
    }

    public String passwordResetLimitKey(String resetKey) {
        return AppConstants.PASSWORD_RESET_LIMIT_KEY + resetKey;
    }

    public String generateUuidForEmailVerification(String email) {
        String raw = UUID.randomUUID().toString() + "-" + email;
        return DigestUtils.sha256Hex(raw);

    }

    public String generateUuidForResetPasswordEmail(String email) {
        String raw = UUID.randomUUID().toString() + "-" + email;
        return DigestUtils.sha256Hex(raw);
    }
}
