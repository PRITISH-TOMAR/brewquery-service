package club.sqlhub.Repository.remoteRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;

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
