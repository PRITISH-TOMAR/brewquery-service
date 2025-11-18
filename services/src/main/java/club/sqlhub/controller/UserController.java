package club.sqlhub.controller;

import org.springframework.web.bind.annotation.RestController;

import club.sqlhub.entity.user.DBO.UserDetailsDBO;
import club.sqlhub.entity.user.DTO.RegisterUserDTO;
import club.sqlhub.entity.user.DTO.UserDetailsDTO;
import club.sqlhub.entity.user.DTO.UserLoginDTO;
import club.sqlhub.entity.utlities.UserJWTDetailsDBO;
import club.sqlhub.service.UserService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDetailsDTO>> RegisterUser(@Valid @RequestBody RegisterUserDTO req) {
        return userService.registerUser(req);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserJWTDetailsDBO>> RegisterUser(@Valid @RequestBody UserLoginDTO req) {
        return userService.loginUser(req);
    }
}
