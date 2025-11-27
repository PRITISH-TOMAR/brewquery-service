package club.sqlhub.utils.APiResponse;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private String error;
    private List<T> dataList;

    public ApiResponse(HttpStatus httpStatus, String message) {
        this.status = httpStatus.value();
        this.message = message;
    }

    public ApiResponse(HttpStatus httpStatus, String message, Exception err) {
        this.status = httpStatus.value();
        this.message = message;
        this.error = err.getMessage();
    }

    public ApiResponse(HttpStatus httpStatus, String message, T data) {
        this.status = httpStatus.value();
        this.message = message;
        this.data = data;
    }

    public ApiResponse(HttpStatus httpStatus, String message, List<T> dataList) {
        this.status = httpStatus.value();
        this.message = message;
        this.dataList = dataList;
    }

    public static <T> ResponseEntity<ApiResponse<T>> call(HttpStatus httpStatus, String message) {
        ApiResponse<T> apiResponse = new ApiResponse<>(httpStatus, message);
        return ResponseEntity.status(httpStatus).body(apiResponse);
    }

    public static <T> ResponseEntity<ApiResponse<T>> call(HttpStatus httpStatus, String message, T data) {
        ApiResponse<T> apiResponse = new ApiResponse<>(httpStatus, message, data);
        return ResponseEntity.status(httpStatus).body(apiResponse);
    }

    public static <T> ResponseEntity<ApiResponse<T>> call(HttpStatus httpStatus, String message, List<T> dataList) {
        ApiResponse<T> apiResponse = new ApiResponse<>(httpStatus, message, dataList);
        return ResponseEntity.status(httpStatus).body(apiResponse);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus httpStatus, String message, Exception err) {
        ApiResponse<T> apiResponse = new ApiResponse<>(httpStatus, message, err);
        return ResponseEntity.status(httpStatus).body(apiResponse);
    }


}