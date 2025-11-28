package club.sqlhub.utils.remoteServiceHelper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SQLRemoteApiHelper {
        private final WebClient client;

        public ResponseEntity<ApiResponse<Object>> post(String uri, Object body) {
                try {
                        ApiResponse<?> raw = client.post()
                                        .uri(uri)
                                        .bodyValue(body)
                                        .retrieve()
                                        .bodyToMono(ApiResponse.class)
                                        .block();

                        if (raw == null) {
                                return ApiResponse.call(
                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Null response from engine");
                        }

                        return ApiResponse.call(
                                        HttpStatus.valueOf(raw.getStatus()),
                                        raw.getMessage(),
                                        raw.getData());

                } catch (WebClientResponseException e) {
                        // Try to parse the error response body as ApiResponse
                        try {
                                ObjectMapper mapper = new ObjectMapper();
                                ApiResponse<?> errorResponse = mapper.readValue(
                                                e.getResponseBodyAsString(),
                                                ApiResponse.class);

                                return ApiResponse.call(
                                                HttpStatus.valueOf(errorResponse.getStatus()),
                                                errorResponse.getMessage(),
                                                errorResponse.getData());

                        } catch (Exception parseException) {
                                // If parsing fails, use the exception's status code
                                return ApiResponse.call(
                                                HttpStatus.valueOf(e.getStatusCode().value()),
                                                e.getMessage(),
                                                e.getResponseBodyAsString());
                        }

                } catch (Exception e) {
                        return ApiResponse.call(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        e.getMessage(),
                                        null);
                }
        }
}
