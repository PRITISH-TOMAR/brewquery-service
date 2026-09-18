package club.sqlhub.Repository.remoteRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.entity.judge.JudgeServerJobDTO.JudgeJobPayload;
import club.sqlhub.entity.judge.JudgeServerJobDTO.RunTestcaseResponseDTO;
import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionStatusResponseDTO;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.remoteServiceHelper.SQLRemoteApiHelper;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class SQLRemoteRepository {

    private final SQLRemoteApiHelper apiHelper;
    private final ObjectMapper mapper;

    @Value("${judge.execute-url:http://localhost:8081/jobs}")
    private String EXECUTE_SQL_URL;

    @Value("${judge.run-testcases-url:http://localhost:8081/jobs/test}")
    private String RUN_PUBLIC_TESTCASES_URL;

    public SubmissionStatusResponseDTO submitQuery(JudgeJobPayload payload) {

        ResponseEntity<ApiResponse<Object>> response = apiHelper.post(EXECUTE_SQL_URL, payload);

        ApiResponse<Object> body = response.getBody();
        if (body == null || body.getData() == null) {
            SubmissionStatusResponseDTO resError = new SubmissionStatusResponseDTO();
            resError.setStatus("Failed");
            return resError;
        }
        SubmissionStatusResponseDTO desData = mapper.convertValue(body.getData(), SubmissionStatusResponseDTO.class);
        return desData;
    }

    public RunTestcaseResponseDTO runPublicTestCases(JudgeJobPayload payload) {

        ResponseEntity<ApiResponse<Object>> response = apiHelper.post(RUN_PUBLIC_TESTCASES_URL, payload);

        ApiResponse<Object> body = response.getBody();
        if (body == null || body.getData() == null) {
            RunTestcaseResponseDTO resError = new RunTestcaseResponseDTO();
            resError.setOverallStatus("Failed");
            return resError;
        }
        RunTestcaseResponseDTO desData = mapper.convertValue(body.getData(), RunTestcaseResponseDTO.class);
        return desData;
    }

}
