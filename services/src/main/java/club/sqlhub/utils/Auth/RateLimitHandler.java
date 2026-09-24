package club.sqlhub.utils.Auth;

import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import club.sqlhub.constants.AppConstants;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitHandler {

    public String limitKey(String email) {
        return AppConstants.REDIS_OTP_RATE_LIMIT__KEY + email;
    }

    public String passwordResetKey(String key) {
        return AppConstants.PASSWORD_RESET_KEY + key;
    }

    public String passwordResetLimitKey(String resetKey) {
        return AppConstants.PASSWORD_RESET_LIMIT_KEY + resetKey;
    }

    public String generateUuidForResetPasswordEmail(String email) {
        String raw = UUID.randomUUID().toString() + "-" + email;
        return DigestUtils.sha256Hex(raw);
    }
}
