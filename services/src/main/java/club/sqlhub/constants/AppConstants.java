package club.sqlhub.constants;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class AppConstants {

    public static final String NOT_FOUND = "This site can't be reached.";
    public static final Integer OTP_REQUEST_LIMIT = 5;
    public static final Integer OTP_WINDOW_MINUTES = 10;
    public static final Integer OTP_TTL_MINUTES = 5;
    public static String REDIS_OTP_KEY = "OTP:";
    public static String REDIS_OTP_RATE_LIMIT__KEY = "OTP_REQ:";
    public static String PASSWORD_ALGO_KEY = "PBKDF2WithHmacSHA256";
    public static String REDIS_EMAIL_VERIFICATION_KEY = "EMAIL_VERIFY:";
    public static String PASSWORD_RESET_KEY = "RESET:";
    public static String PASSWORD_RESET_LIMIT_KEY = "RESET_REQ:";
    public static final Integer PASSWORD_RESET_WINDOW_MINUTES = 10;

    // USER ENTITIES CONSTANTS
    public static String DEFAULT_STATUS = "ACTIVE";
    public static Integer DEFAULT_ROLE_ID = 1;
    public static String DEFAULT_PROFILE_PICTURE_PATH = "";

    // JWT
    public static final long ACCESS_TOKEN_TTL_MS = 60 * 60 * 1000L; // 1 hour
    public static final long REFRESH_TOKEN_TTL_MS = 15 * 60 * 60 * 24 * 1000L; // 15 days

    // EMAIL TEMPLATES SUBJECT
    public static  String EMAIL_SUBJECT_OTP = "OTP for Email Verification";
    public static  String EMAIL_SUBJECT_PASSWORD_RESET = "Password Reset Link";

    // Remote API's links
    public static final String LOAD_DATASET = "/sql/load";
    public static final String EXECUTE_SQL = "/sql/execute";
}
