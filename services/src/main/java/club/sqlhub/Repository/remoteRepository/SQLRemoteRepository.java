package club.sqlhub.Repository.remoteRepository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.entity.judge.JudgeServerJobDTO.JudgeJobPayload;
import club.sqlhub.entity.judge.JudgeServerJobDTO.RunTestcaseResponseDTO;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.remoteServiceHelper.SQLRemoteApiHelper;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SQLRemoteRepository {

    private final SQLRemoteApiHelper apiHelper;
    private final ObjectMapper mapper;

    @Value("${engine.base-url}")
    private String engineBaseUrl;

    @Value("${judge.execute-url}")
    private String EXECUTE_SQL_URL;

    @Value("${judge.run-testcases-url}")
    private String RUN_PUBLIC_TESTCASES_URL;

    public RunTestcaseResponseDTO submitQuery(JudgeJobPayload payload) {

        ResponseEntity<ApiResponse<Object>> response = apiHelper.post(EXECUTE_SQL_URL, payload);

        ApiResponse<Object> body = response.getBody();
        if (body == null || body.getData() == null || body.getStatus() >= 400) {
            RunTestcaseResponseDTO resError = new RunTestcaseResponseDTO();
            resError.setOverallStatus(body != null ? body.getMessage() : "Engine unavailable");
            return resError;
        }
        return mapper.convertValue(body.getData(), RunTestcaseResponseDTO.class);
    }

    // ── Engine session endpoints ───────────────────────────────────────────────

    /**
     * Loads a dataset into an ephemeral H2 session on the engine.
     * Throws if the engine returns an error.
     */
    public void loadDataset(String sessionId, String datasetId, String sqlMode) {
        Map<String, String> body = new HashMap<>();
        body.put("sessionId", sessionId);
        body.put("datasetId", datasetId);
        body.put("sqlMode", sqlMode != null ? sqlMode : "MySQL");
        ResponseEntity<ApiResponse<Object>> res = apiHelper.post(engineBaseUrl + "/engine/sql/load", body);
        ApiResponse<Object> rb = res.getBody();
        if (rb == null || rb.getStatus() >= 400) {
            throw new RuntimeException("Engine /load failed: " + (rb != null ? rb.getMessage() : "null response"));
        }
    }

    /**
     * Executes a SELECT query against a loaded H2 session.
     * Returns { columns, rows, rowsCount } or null on failure.
     */
    public Map<String, Object> executeQuery(String sessionId, String query) {
        Map<String, String> body = new HashMap<>();
        body.put("sessionId", sessionId);
        body.put("query", query);
        body.put("type", "DQL");
        ResponseEntity<ApiResponse<Object>> res = apiHelper.post(engineBaseUrl + "/engine/sql/execute", body);
        ApiResponse<Object> rb = res.getBody();
        if (rb == null || rb.getData() == null || rb.getStatus() >= 400) return null;
        return mapper.convertValue(rb.getData(), new TypeReference<Map<String, Object>>() {});
    }

    public RunTestcaseResponseDTO runPublicTestCases(JudgeJobPayload payload) {

        ResponseEntity<ApiResponse<Object>> response = apiHelper.post(RUN_PUBLIC_TESTCASES_URL, payload);

        ApiResponse<Object> body = response.getBody();
        if (body == null || body.getData() == null || body.getStatus() >= 400) {
            RunTestcaseResponseDTO resError = new RunTestcaseResponseDTO();
            resError.setOverallStatus(body != null ? body.getMessage() : "Engine unavailable");
            return resError;
        }
        return mapper.convertValue(body.getData(), RunTestcaseResponseDTO.class);
    }

}
