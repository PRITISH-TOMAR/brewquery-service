package club.sqlhub.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.entity.judge.JudgeServerJobDTO.RunTestcaseResponseDTO;
import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionResponseDTO;
import club.sqlhub.entity.judge.SubmissionRequestDTO;
import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.mongo.models.Question;
import club.sqlhub.mongo.repository.DatasetRepository;
import club.sqlhub.mongo.service.QuestionService;
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
    private final QuestionService questionService;
    private final DatasetRepository datasetRepository;

    private static final String RESULT_PREFIX = "result:sql:";
    private static final String META_PREFIX   = "meta:sql:";

    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> expectedOutput(String jobId) {
        try {
            // 1. Check Redis — engine worker writes result here
            String resultJson = stringRedisTemplate.opsForValue().get(RESULT_PREFIX + jobId);

            if (resultJson != null) {
                RunTestcaseResponseDTO runResult =
                        objectMapper.readValue(resultJson, RunTestcaseResponseDTO.class);

                // Parse meta JSON for userId + questionId
                String userId = null;
                String questionId = null;
                String metaJson = stringRedisTemplate.opsForValue().get(META_PREFIX + jobId);
                if (metaJson != null) {
                    try {
                        Map<String, String> meta = objectMapper.readValue(metaJson, new TypeReference<>() {});
                        userId = meta.get("userId");
                        questionId = meta.get("questionId");
                    } catch (Exception ignored) {
                        userId = metaJson; // legacy plain-string fallback
                    }
                }

                // Enrich with question + dataset info
                String questionTitle = null, datasetTitle = null, level = null;
                if (questionId != null) {
                    Question q = questionService.getByIdRaw(questionId);
                    if (q != null) {
                        questionTitle = q.getTitle();
                        level = q.getDifficulty();
                        Dataset ds = datasetRepository.findById(q.getDatasetId()).orElse(null);
                        if (ds != null) datasetTitle = ds.getTitle();
                    }
                }

                // Persist to MongoDB for history (upsert — jobId is @Id)
                JudgeResultDTO dto = new JudgeResultDTO();
                dto.setJobId(jobId);
                dto.setUserId(userId);
                dto.setQuestionId(questionId);
                dto.setQuestionTitle(questionTitle);
                dto.setDataset(datasetTitle);
                dto.setLevel(level);
                dto.setLanguage("SQL");
                dto.setSubmittedAt(new Date());
                dto.setResult(runResult);
                userQueriesResultService.save(dto);

                SubmissionResponseDTO response = toSubmissionResponse(runResult);
                response.setId(jobId);
                response.setUserId(userId);
                response.setQuestionId(questionId);
                response.setQuestionTitle(questionTitle);
                response.setDataset(datasetTitle);
                response.setLevel(level);
                response.setLanguage("SQL");
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
            pending.setResult("PENDING");
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
        res.setTimeTaken(r.getTotalExecutionMs() + " ms");
        res.setTestDetails(r.getTestDetails());
        res.setResult(mapVerdict(r.getOverallStatus()));
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
