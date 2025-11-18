package club.sqlhub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import club.sqlhub.service.HomeService;
import club.sqlhub.utils.APiResponse.ApiResponse;

@RestController
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<String>> home() {
        return homeService.home();
    }
}
