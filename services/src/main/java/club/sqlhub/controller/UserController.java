package club.sqlhub.controller;

import org.springframework.web.bind.annotation.RestController;

import club.sqlhub.entity.user.UserDetailsDBO;
import club.sqlhub.service.UserService;
import club.sqlhub.utils.APiResponse.ApiResponse;

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
    public ResponseEntity<ApiResponse<UserDetailsDBO>> RegisterUser(@RequestBody UserDetailsDBO req) {
        return userService.registerUser(req);
    }
}
