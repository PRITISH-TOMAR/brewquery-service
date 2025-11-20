package club.sqlhub.service;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import club.sqlhub.Repository.UserRepository;
import club.sqlhub.constants.AppConstants;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.user.DBO.UserDetailsDBO;
import club.sqlhub.entity.utlities.EmailVerifyDTO;
import club.sqlhub.entity.utlities.OTPDBO;
import club.sqlhub.queries.UserQueries;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.Auth.OtpHandler;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OtpHandler otpHandler;
    private final UserQueries userQueries;
    private final UserRepository userRepository;

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

            List<UserDetailsDBO> existUser = userRepository.userExists(email, userQueries.IF_USER_EXISTS);

            if (!existUser.isEmpty()) {
                return ApiResponse.call(HttpStatus.CONFLICT, MessageConstants.USER_ALREADY_EXISTS);
            }
            if (!checkCooldownForEmail(email)) {
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.TOO_MANY_REQUESTS);
            }
            String otp = otpHandler.generateOTP();
            String otpKey = otpHandler.otpKey(email);
            redisTemplate.opsForValue().set(otpKey, otp, Duration.ofMinutes(AppConstants.OTP_TTL_MINUTES));

            otpHandler.sendEmailOTP(email, otp);

            return ApiResponse.call(HttpStatus.OK, MessageConstants.OTP_SENT_SUCCESSFULLY);
        } catch (Exception ex) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, ex);

        }
    }

    public ResponseEntity<ApiResponse<EmailVerifyDTO>> verifyOTP(OTPDBO otpdbo) {
        try {
            String otpkey = otpHandler.otpKey(otpdbo.getEmail());
            String emailVerificationKey = otpHandler
                    .emailVerificationKey(otpHandler.generateUuidForEmailVerification(otpdbo.getEmail()));

            String storedOtp = (String) redisTemplate.opsForValue().get(otpkey);

            if (storedOtp == null) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.OTP_EXPIRED);
            }
            if (!storedOtp.equals(otpdbo.getOtp())) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.INVALID_OTP);
            }

            redisTemplate.delete(otpkey);
            redisTemplate.opsForValue().set(emailVerificationKey, otpdbo.getEmail(),
                    Duration.ofMinutes(AppConstants.OTP_TTL_MINUTES));
            EmailVerifyDTO response = new EmailVerifyDTO();
            response.setKey(emailVerificationKey);

            return ApiResponse.call(HttpStatus.OK, MessageConstants.OTP_VERIFIED_SUCCESSFULLY,
                    response);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

}
