package club.sqlhub.utils.User;

import java.util.Random;

import org.springframework.stereotype.Component;

import club.sqlhub.constants.AppConstants;

@Component
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

}
