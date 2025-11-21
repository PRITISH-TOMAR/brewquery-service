package club.sqlhub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import club.sqlhub.entity.user.DTO.RegisterUserDTO;
import club.sqlhub.entity.user.DTO.ResetPasswordDTO;
import club.sqlhub.entity.user.DTO.UserDetailsDTO;
import club.sqlhub.entity.user.DTO.UserLoginDTO;
import club.sqlhub.entity.utlities.EmailVerifyDTO;
import club.sqlhub.entity.utlities.OTPDBO;
import club.sqlhub.entity.utlities.TokenDBO;
import club.sqlhub.entity.utlities.UserJWTDetailsDBO;
import club.sqlhub.service.AuthService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
     @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDetailsDTO>> RegisterUser(@Valid @RequestBody RegisterUserDTO req) {
        return authService.registerUser(req);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserJWTDetailsDBO>> loginUser(@Valid @RequestBody UserLoginDTO req) {
        return authService.loginUser(req);
    }

    @GetMapping("/send")
    public ResponseEntity<ApiResponse<OTPDBO>> sendOTP(@Valid @RequestParam String email) {
        return authService.sendOTP(email);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<EmailVerifyDTO>> verifyOTP(@Valid @RequestBody OTPDBO otpDbo) {
        return authService.verifyOTP(otpDbo);
    }

    @GetMapping("/refresh/{refreshAccessToken}")
    public ResponseEntity<ApiResponse<TokenDBO>> refreshToken(@Valid @PathVariable String refreshAccessToken) {
        return authService.refreshToken(refreshAccessToken);
    }

    @GetMapping("/forgot-password/{email}")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @PathVariable String email) {
        return authService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordDTO data) {
        return authService.resetPassword(data);
    }
}