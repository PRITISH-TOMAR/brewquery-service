package club.sqlhub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import club.sqlhub.entity.utlities.EmailVerifyDTO;
import club.sqlhub.entity.utlities.OTPDBO;
import club.sqlhub.entity.utlities.TokenDBO;
import club.sqlhub.service.AuthService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
}
