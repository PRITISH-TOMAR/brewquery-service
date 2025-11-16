package club.sqlhub.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import club.sqlhub.constants.AppConstants;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.utlities.OTPDBO;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.OTP.OtpHandler;

@Service
public class OTPService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OtpHandler otpHandler;

    public OTPService(RedisTemplate<String, Object> redisTemplate, OtpHandler otpHandler) {
        this.redisTemplate = redisTemplate;
        this.otpHandler = otpHandler;
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

}
