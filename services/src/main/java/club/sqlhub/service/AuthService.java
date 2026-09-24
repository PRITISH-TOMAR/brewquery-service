package club.sqlhub.service;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import club.sqlhub.Repository.AuthRepository;
import club.sqlhub.constants.AppConstants;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.user.DBO.UserDetailsDBO;
import club.sqlhub.entity.user.DTO.RegisterUserDTO;
import club.sqlhub.entity.user.DTO.ResetPasswordDTO;
import club.sqlhub.entity.user.DTO.UserDetailsDTO;
import club.sqlhub.entity.user.DTO.UserLoginDTO;
import club.sqlhub.entity.utlities.EmailVerifyDTO;
import club.sqlhub.entity.utlities.TokenDBO;
import club.sqlhub.entity.utlities.UserJWTDetailsDBO;
import club.sqlhub.entity.utlities.enums.AuthEnum.TokenValidationResult;
import club.sqlhub.queries.AuthQueries;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.Auth.EmailVerificationTokenHandler;
import club.sqlhub.utils.Auth.JWTHandler;
import club.sqlhub.utils.Auth.RateLimitHandler;
import club.sqlhub.utils.User.UserHandler;
import club.sqlhub.utils.emailTemplates.EmailVerifyLinkTemplate;
import club.sqlhub.utils.emailTemplates.PasswordResetTemplate;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthQueries queries;
    private final AuthRepository authRepository;
    private final UserHandler userHandler;
    private final JWTHandler jwtHandler;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordResetTemplate passwordResetTemplate;
    private final RateLimitHandler otpHandler;
    private final EmailService emailService;
    private final EmailVerificationTokenHandler emailVerificationTokenHandler;
    private final EmailVerifyLinkTemplate emailVerifyLinkTemplate;

    @Transactional
    public ResponseEntity<ApiResponse<UserDetailsDTO>> registerUser(RegisterUserDTO user) {
        try {
            String emailKey = user.getEmailKey().getKey();
            String[] parts = emailVerificationTokenHandler.decrypt(emailKey);
            String tokenEmail = parts[0];
            long expiresAt = Long.parseLong(parts[1]);

            if (System.currentTimeMillis() > expiresAt) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST,
                        MessageConstants.EMAIL_VERFICATION_KEY_EXPIRED);
            }

            if (!tokenEmail.equalsIgnoreCase(user.getEmail())) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST,
                        MessageConstants.EMAIL_VERFICATION_KEY_EXPIRED);
            }

            UserDetailsDBO newUser = userHandler.convertRegisterDtoToDbo(user);

            String salt = userHandler.generateSalt();
            String hashedPassword = userHandler.hashPassword(user.getPassword(), salt);

            newUser.setHashedPassword(hashedPassword);
            newUser.setSalt(salt);

            UserDetailsDTO createdUser = authRepository.addUser(
                    newUser,
                    queries.INSERT_USER_DETAILS);

            return ApiResponse.call(HttpStatus.CREATED,
                    MessageConstants.USER_CREATED_SUCCESSFULLY,
                    createdUser);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<UserJWTDetailsDBO>> loginUser(UserLoginDTO user) {
        try {
            List<UserDetailsDBO> existUser = authRepository.userExists(user.getEmail(), queries.IF_USER_EXISTS);

            if (existUser.isEmpty()) {
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.USER_NOT_FOUND);
            }

            boolean validPassword = userHandler.validPassword(
                    user.getPassword(),
                    existUser.get(0).getHashedPassword(),
                    existUser.get(0).getSalt());

            if (!validPassword)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.USER_NOT_FOUND);

            String subject = String.valueOf(existUser.get(0).getUserId());

            UserJWTDetailsDBO resUser = jwtHandler.buildUserJWTDetails(subject, existUser.get(0), user.getRememberMe());

            return ApiResponse.call(HttpStatus.OK,
                    MessageConstants.USER_LOGIN_SUCCESSFULLY, resUser);

        } catch (Exception ex) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR, ex);
        }
    }

    public boolean checkCooldownForEmail(String email) {
        String limitKey = otpHandler.limitKey(email);
        Integer count = (Integer) redisTemplate.opsForValue().get(limitKey);

        if (count == null) {
            redisTemplate.opsForValue().set(limitKey, 1,
                    Duration.ofMinutes(AppConstants.OTP_WINDOW_MINUTES));
            return true;
        }

        if (count >= AppConstants.OTP_REQUEST_LIMIT) {
            return false;
        }

        redisTemplate.opsForValue().increment(limitKey);
        return true;
    }

    public ResponseEntity<ApiResponse<Void>> sendVerificationLink(String email) {
        try {
            List<UserDetailsDBO> existUser = authRepository.userExists(email, queries.IF_USER_EXISTS);

            if (!existUser.isEmpty()) {
                return ApiResponse.call(HttpStatus.CONFLICT, MessageConstants.USER_ALREADY_EXISTS);
            }

            if (!checkCooldownForEmail(email)) {
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.TOO_MANY_REQUESTS);
            }

            long expiresAt = System.currentTimeMillis() +
                    Duration.ofMinutes(AppConstants.VERIFY_LINK_TTL_MINUTES).toMillis();

            String token = emailVerificationTokenHandler.encrypt(email, expiresAt);

            emailService.sendEmail(
                    email,
                    AppConstants.EMAIL_SUBJECT_VERIFY,
                    emailVerifyLinkTemplate.getVerifyLinkTemplate(token));

            return ApiResponse.call(HttpStatus.OK, MessageConstants.VERIFICATION_LINK_SENT);

        } catch (Exception ex) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR, ex);
        }
    }

    public ResponseEntity<ApiResponse<EmailVerifyDTO>> verifyEmailLink(String token) {
        try {
            String[] parts = emailVerificationTokenHandler.decrypt(token);
            String email = parts[0];
            long expiresAt = Long.parseLong(parts[1]);

            if (System.currentTimeMillis() > expiresAt) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.VERIFICATION_LINK_EXPIRED);
            }

            long emailKeyExpiresAt = System.currentTimeMillis() +
                    Duration.ofMinutes(AppConstants.EMAIL_KEY_TTL_MINUTES).toMillis();

            String emailKey = emailVerificationTokenHandler.encrypt(email, emailKeyExpiresAt);

            EmailVerifyDTO response = new EmailVerifyDTO();
            response.setKey(emailKey);
            response.setEmail(email);

            return ApiResponse.call(HttpStatus.OK,
                    MessageConstants.EMAIL_VERIFIED_SUCCESSFULLY,
                    response);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<TokenDBO>> refreshToken(String refreshAccessToken) {
        try {
            TokenValidationResult validationResult = jwtHandler.validateToken(refreshAccessToken);

            if (validationResult == TokenValidationResult.VALID) {
                String subject = jwtHandler.extractSubject(refreshAccessToken);
                String newAccessToken = jwtHandler.generateToken(subject);

                TokenDBO tokenDbo = new TokenDBO();
                tokenDbo.setAccessToken(newAccessToken);

                return ApiResponse.call(HttpStatus.OK,
                        MessageConstants.REFRESH_TOKEN_GENERATED,
                        tokenDbo);
            }

            return ApiResponse.call(HttpStatus.UNAUTHORIZED,
                    MessageConstants.REFRESH_TOKEN_EXPIRED);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public boolean checkCooldownForEmailFOrPasswordReset(String resetKey) {
        String limitKey = otpHandler.passwordResetLimitKey(resetKey);
        Integer count = (Integer) redisTemplate.opsForValue().get(limitKey);

        if (count == null) {
            redisTemplate.opsForValue().set(
                    limitKey, 1,
                    Duration.ofMinutes(AppConstants.PASSWORD_RESET_WINDOW_MINUTES));
            return true;
        }

        if (count >= AppConstants.PASSWORD_RESET_WINDOW_MINUTES) {
            return false;
        }

        redisTemplate.opsForValue().increment(limitKey);
        return true;
    }

    public ResponseEntity<ApiResponse<String>> forgotPassword(String email) {
        try {
            List<UserDetailsDBO> existUser = authRepository.userExists(email, queries.IF_USER_EXISTS);

            if (existUser.isEmpty()) {
                return ApiResponse.call(HttpStatus.OK, MessageConstants.LINK_SENT_TO_EMAIL);
            }

            if (!checkCooldownForEmailFOrPasswordReset(email)) {
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.TOO_MANY_REQUESTS);
            }

            String keyGenerated = otpHandler.generateUuidForResetPasswordEmail(email);
            String resetKey = otpHandler.passwordResetKey(keyGenerated);

            redisTemplate.opsForValue().set(resetKey, email,
                    Duration.ofMinutes(AppConstants.OTP_TTL_MINUTES));

            emailService.sendEmail(
                    email,
                    AppConstants.EMAIL_SUBJECT_PASSWORD_RESET,
                    passwordResetTemplate.getPasswordResetTemplate(keyGenerated));

            return ApiResponse.call(HttpStatus.OK, MessageConstants.LINK_SENT_TO_EMAIL);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<String>> resetPassword(ResetPasswordDTO data) {
        try {
            String resetKey = otpHandler.passwordResetKey(data.getKey());
            String storedEmail = (String) redisTemplate.opsForValue().get(resetKey);

            if (storedEmail == null) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.RESET_LINK_EXPIRED);
            }

            String salt = userHandler.generateSalt();
            String hashedPassword = userHandler.hashPassword(data.getPassword(), salt);

            UserDetailsDBO user = new UserDetailsDBO();
            user.setHashedPassword(hashedPassword);
            user.setSalt(salt);
            user.setEmail(storedEmail);

            authRepository.resetPassword(user, queries.RESET_PASSWORD_QUERY);
            redisTemplate.delete(resetKey);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.PASSWORD_RESET_SUCCESSFULLY);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ApiResponse.call(HttpStatus.OK, MessageConstants.LOGGED_OUT_SUCCESSFULLY);
            }

            String token = authHeader.substring(7);
            TokenValidationResult result = jwtHandler.validateToken(token);

            // Already expired or invalid — nothing to blacklist
            if (result != TokenValidationResult.VALID) {
                return ApiResponse.call(HttpStatus.OK, MessageConstants.LOGGED_OUT_SUCCESSFULLY);
            }

            Date expiration = jwtHandler.extractAllClaims(token).getExpiration();
            long remainingMs = expiration.getTime() - System.currentTimeMillis();

            if (remainingMs > 0) {
                String blacklistKey = AppConstants.REDIS_TOKEN_BLACKLIST_KEY + token;
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", Duration.ofMillis(remainingMs));
            }

            return ApiResponse.call(HttpStatus.OK, MessageConstants.LOGGED_OUT_SUCCESSFULLY);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<String>> resetPasswordPing(String key) {
        try {
            String resetKey = otpHandler.passwordResetKey(key);
            String storedEmail = (String) redisTemplate.opsForValue().get(resetKey);

            if (storedEmail == null) {
                return ApiResponse.call(HttpStatus.GONE, MessageConstants.RESET_LINK_EXPIRED);
            }
            return ApiResponse.call(HttpStatus.OK, MessageConstants.RESET_LINK_VALID);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

}
