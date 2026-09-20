package club.sqlhub.service;

import java.time.Duration;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.entity.judge.JudgeServerJobDTO.RunTestcaseResponseDTO;
import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionResponseDTO;
import club.sqlhub.entity.judge.SubmissionRequestDTO;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.mongo.service.UserQueriesResultService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.converter.JudgeResponseConverter;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
@AllArgsConstructor
public class JudgeService {
    private final UserQueriesResultService userQueriesResultService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String RESULT_PREFIX = "result:sql:";
    private static final String META_PREFIX   = "meta:sql:";

    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> expectedOutput(String jobId) {
        try {
            // 1. Check Redis — engine worker writes result here
            String resultJson = stringRedisTemplate.opsForValue().get(RESULT_PREFIX + jobId);

            if (resultJson != null) {
                RunTestcaseResponseDTO runResult =
                        objectMapper.readValue(resultJson, RunTestcaseResponseDTO.class);

                // Persist to MongoDB for history (upsert — jobId is @Id)
                String userId = stringRedisTemplate.opsForValue().get(META_PREFIX + jobId);
                JudgeResultDTO dto = new JudgeResultDTO();
                dto.setJobId(jobId);
                dto.setUserId(userId);
                dto.setResult(runResult);
                userQueriesResultService.save(dto);

                SubmissionResponseDTO response = toSubmissionResponse(runResult);
                return ApiResponse.call(HttpStatus.OK, "Job result fetched successfully", response);
            }

            // 2. Fallback — already persisted to MongoDB (Redis TTL expired)
            JudgeResultDTO persisted = userQueriesResultService.findById(jobId);
            if (persisted != null && persisted.getResult() != null) {
                SubmissionResponseDTO response =
                        JudgeResponseConverter.convertJudgeResultToSubmissionResponse(persisted);
                return ApiResponse.call(HttpStatus.OK, "Job result fetched successfully", response);
            }

            // 3. Still processing
            SubmissionResponseDTO pending = new SubmissionResponseDTO();
            pending.setVerdict("PENDING");
            return ApiResponse.call(HttpStatus.OK, "Job still processing", pending);

        } catch (Exception ex) {
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal error while fetching job result",
                    ex);
        }
    }

    private SubmissionResponseDTO toSubmissionResponse(RunTestcaseResponseDTO r) {
        SubmissionResponseDTO res = new SubmissionResponseDTO();
        res.setPassCount(r.getPassedCount());
        res.setTotalCount(r.getTotalCount());
        res.setExecutionTime(r.getTotalExecutionMs() + " ms");
        res.setTestDetails(r.getTestDetails());
        res.setType("submission");
        res.setVerdict(mapVerdict(r.getOverallStatus()));
        return res;
    }

    private String mapVerdict(String overallStatus) {
        if (overallStatus == null) return "UNKNOWN";
        switch (overallStatus) {
            case "PASS":    return "ACCEPTED";
            case "FAIL":    return "WRONG_ANSWER";
            case "PARTIAL": return "PARTIAL_ACCEPTED";
            default:        return overallStatus;
        }
    }

    public ResponseEntity<ApiResponse<List<SubmissionResponseDTO>>> findSubmissionsPerUserByFilters(SubmissionRequestDTO req) {
        try {
            Page<SubmissionResponseDTO> judgeResultDTO = userQueriesResultService.findByFilters(req);

            List<SubmissionResponseDTO> list = judgeResultDTO.getContent();
            return ApiResponse.call(
                    HttpStatus.OK,
                    "Submissions fetched successfully",
                    list);
        } catch (Exception ex) {
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal error while fetching submissions",
                    ex);
        }
    }

}
