package club.sqlhub.Repository.remoteRepository;

import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.entity.coreEngine.JudgeServerJobDTO.CompareQueryDTO;
import club.sqlhub.entity.coreEngine.JudgeServerJobDTO.JudgeJobPayload;
import club.sqlhub.entity.coreEngine.JudgeServerJobDTO.JudgeResponseDTO;
import club.sqlhub.entity.coreEngine.JudgeServerJobDTO.SubmissionStatusResponseDTO;
import club.sqlhub.mongo.models.ExpectedSolution;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.remoteServiceHelper.SQLRemoteApiHelper;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class SQLRemoteRepository {

    private final RestTemplate restTemplate;
    private final SQLRemoteApiHelper apiHelper;
    private final ObjectMapper mapper;
    private final String EXECUTE_SQL_URL = "http://localhost:8081/jobs";
    private final String EXECUTE_JUDGE_URL = "http://localhost:8081/jobs/result";

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

    public JudgeResponseDTO evaluateSubmission(String type, JudgeJobPayload jobPayload) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<JudgeJobPayload> request = new HttpEntity<>(jobPayload, headers);

        ResponseEntity<ApiResponse> resp = restTemplate.exchange(
                EXECUTE_JUDGE_URL,
                HttpMethod.POST,
                request,
                ApiResponse.class);

        if (resp == null || resp.getBody() == null) {
            return null;
        }

        JudgeResponseDTO result = restTemplate.getForObject(EXECUTE_JUDGE_URL, JudgeResponseDTO.class);

        return result;
    }
    

    

    public CompareQueryDTO toCompareDTO(ExpectedSolution.OutputData data) {
        CompareQueryDTO dto = new CompareQueryDTO();
        dto.setColumns(data.getColumns());
        dto.setRows(data.getRows());
        dto.setRowsCount(data.getRows() == null ? 0 : data.getRows().size());
        return dto;
    }

}
