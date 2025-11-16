package club.sqlhub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import club.sqlhub.entity.utlities.OTPDBO;
import club.sqlhub.service.OTPService;
import club.sqlhub.utils.APiResponse.ApiResponse;

@RestController
@RequestMapping("otp")
public class OTPController {
    private final OTPService otpService;

    public OTPController(OTPService otpService) {
        this.otpService = otpService;
    }

    @GetMapping("/send")
    public ResponseEntity<ApiResponse<OTPDBO>> sendOTP(@RequestParam String email){
        return otpService.sendOTP(email);
    }

}
