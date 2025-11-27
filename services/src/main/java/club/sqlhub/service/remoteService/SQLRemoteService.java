package club.sqlhub.service.remoteService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SQLRemoteService {
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

                } catch (Exception ex) {
                        return ApiResponse.error(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        "ENGINE_CALL_FAILED",
                                        ex);
                }
        }

}
