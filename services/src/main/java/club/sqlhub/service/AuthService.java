package club.sqlhub.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import club.sqlhub.Repository.AuthRepository;
import club.sqlhub.constants.AppConstants;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.utlities.OTPDBO;
import club.sqlhub.queries.AuthQueries;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.Auth.OtpHandler;

@Service
public class AuthService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OtpHandler otpHandler;
    private final AuthQueries authQueries;
    private final AuthRepository authRepository;

    public AuthService(RedisTemplate<String, Object> redisTemplate, OtpHandler otpHandler, AuthQueries authQueries,
            AuthRepository authRepository) {
        this.redisTemplate = redisTemplate;
        this.otpHandler = otpHandler;
        this.authRepository = authRepository;
        this.authQueries = authQueries;
    }

    public boolean checkCooldownForEmail(String email) {

        String limitKey = otpHandler.limitKey(email);
        Integer count = (Integer) redisTemplate.opsForValue().get(limitKey);

        // FIRST REQUEST
        if (count == null) {
            redisTemplate.opsForValue().set(
                    limitKey,
                    1,
                    Duration.ofMinutes(AppConstants.OTP_WINDOW_MINUTES));
            return true; // allowed
        }

        // TOO MANY REQUESTS
        if (count >= AppConstants.OTP_REQUEST_LIMIT) {
            return false;
        }

        redisTemplate.opsForValue().increment(limitKey);
        return true;
    }

    public ResponseEntity<ApiResponse<OTPDBO>> sendOTP(String email) {
        try {
            if (!checkCooldownForEmail(email)) {
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.TOO_MANY_REQUESTS);
            }
            String otp = otpHandler.generateOTP();
            String otpKey = otpHandler.otpKey(email);
            redisTemplate.opsForValue().set(otpKey, otp, Duration.ofMinutes(AppConstants.OTP_TTL_MINUTES));

            // Send OTP Mail service
            otpHandler.sendEmailOTP(email, otp);

            return ApiResponse.call(HttpStatus.OK, MessageConstants.OTP_SENT_SUCCESSFULLY);
        } catch (Exception ex) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, ex);

        }
    }

    public ResponseEntity<ApiResponse<String>> verifyOTP(OTPDBO otpdbo) {
        try {
            String otpkey = otpHandler.otpKey(otpdbo.getEmail());
            String storedOtp = (String) redisTemplate.opsForValue().get(otpkey);

            if (storedOtp == null) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.OTP_EXPIRED);
            }
            if (!storedOtp.equals(otpdbo.getOtp())) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.INVALID_OTP);
            }

            // handle lgic to save in db
            authRepository.validateUser(otpdbo.getEmail(), authQueries.ValidateUser);
            redisTemplate.delete(otpkey);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.OTP_VERIFIED_SUCCESSFULLY);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

}
