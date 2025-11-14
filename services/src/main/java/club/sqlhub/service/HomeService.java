package club.sqlhub.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import club.sqlhub.constants.AppConstants;
import club.sqlhub.utils.ApiResponse;

@Service
public class HomeService {

    public ResponseEntity<ApiResponse<String>> home() {
        return  ApiResponse.call(
                HttpStatus.NOT_FOUND,
                AppConstants.NOT_FOUND);
    }
}