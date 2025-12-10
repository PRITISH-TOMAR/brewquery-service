package club.sqlhub.utils.remoteServiceHelper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SQLRemoteApiHelper {

    private final WebClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public ResponseEntity<ApiResponse<Object>> post(String uri, Object body) {

        try {
            // -----------------------------------------
            // SUCCESS RESPONSE
            // -----------------------------------------
            String rawJson = client.post()
                    .uri(uri)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (rawJson == null) {
                return ApiResponse.call(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Runner returned NULL response");
            }

            // Try reading ApiResponse cleanly
            try {
                ApiResponse<?> parsed = mapper.readValue(rawJson, ApiResponse.class);
                return ApiResponse.call(
                        HttpStatus.valueOf(parsed.getStatus()),
                        parsed.getMessage(),
                        parsed.getData()
                );
            } catch (Exception ignore) {
                // Not ApiResponse format, fall through
            }

            // Try generic JSON node
            try {
                JsonNode node = mapper.readTree(rawJson);

                String message =
                        node.has("message") ? node.get("message").asText() :
                        node.has("error")   ? node.get("error").asText() :
                        rawJson;

                return ApiResponse.call(
                        HttpStatus.OK,
                        message,
                        node
                );
            } catch (Exception ignore2) {
                // Not JSON, fall through
            }

            // Plain text response
            return ApiResponse.call(
                    HttpStatus.OK,
                    rawJson,
                    rawJson
            );

        } catch (WebClientResponseException e) {

            String rawBody = e.getResponseBodyAsString();

            // -----------------------------------------
            // ERROR RESPONSE
            // -----------------------------------------
            try {
                JsonNode node = mapper.readTree(rawBody);
                String message =
                        node.has("message") ? node.get("message").asText() :
                        node.has("error") ? node.get("error").asText() :
                        rawBody;

                return ApiResponse.call(
                        HttpStatus.OK,
                        message,
                        node
                );
            } catch (Exception ignore) {
                return ApiResponse.call(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        rawBody,
                        rawBody
                );
            }

        } catch (Exception e) {
            // -----------------------------------------
            // LOCAL INTERNAL ERROR
            // -----------------------------------------
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Remote API Error: " + e.getMessage(),
                    e
            );
        }
    }
}
