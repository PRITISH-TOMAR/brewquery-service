package club.sqlhub.constants;

import org.springframework.stereotype.Component;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class AppConstants {
    private final Dotenv dotenv;

    public static final String NOT_FOUND = "This site can't be reached.";
    public static final Integer OTP_REQUEST_LIMIT = 5;
    public static final Integer OTP_WINDOW_MINUTES = 10;
    public static final Integer OTP_TTL_MINUTES = 5;
    public static String REDIS_OTP_KEY = "OTP:";
    public static String REDIS_OTP_RATE_LIMIT__KEY = "OTP_REQ:";
    public static String PASSWORD_ALGO_KEY = "PBKDF2WithHmacSHA256";

    // USER ENTITIES CONSTANTS
    public static String DEFAULT_STATUS = "IN_PROGRESS";
    public static Integer DEFAULT_ROLE_ID = 1;
    public static String DEFAULT_PROFILE_PICTURE_PATH = "";

    // JWT
    public static final long ACCESS_TOKEN_TTL_MS = 60 * 60 * 1000L; // 1 hour
    public static final long REFRESH_TOKEN_TTL_MS = 7L * 24 * 60 * 60 * 1000L;
}
