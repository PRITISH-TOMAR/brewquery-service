package club.sqlhub.service.remoteService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.Repository.remoteRepository.SQLRemoteRepository;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.Enums.TestCaseType;
import club.sqlhub.entity.judge.JudgeServerJobDTO.JudgeJobPayload;
import club.sqlhub.entity.judge.JudgeServerJobDTO.RunTestcaseResponseDTO;
import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionStatusResponseDTO;
import club.sqlhub.entity.judge.SQLDTO.SQLInputDTO;
import club.sqlhub.entity.judge.SQLDTO.SQLPayload;
import club.sqlhub.mongo.models.Question;
import club.sqlhub.mongo.models.TestCaseSQL.TestCase;
import club.sqlhub.mongo.service.QuestionService;
import club.sqlhub.mongo.service.TestCaseService;
import club.sqlhub.mongo.service.UserQueriesResultService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.remoteServiceHelper.RemoteServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SQLRemoteService {

    private final QuestionService questionService;
    private final TestCaseService testCaseService;
    private final ObjectMapper objectMapper;
    private final SQLRemoteRepository sqlRemoteRepository;
    private final UserQueriesResultService userQueriesResultService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String JUDGE_QUEUE    = "judge:queue:sql";
    private static final String META_PREFIX    = "meta:sql:";
    private static final long   META_TTL_S     = 3600;

    public ResponseEntity<ApiResponse<SubmissionStatusResponseDTO>> executeQuery(
            SQLInputDTO obj,
            String userId) {

        try {
            Question question = questionService.getByIdRaw(obj.getQuestionId());
            if (question == null) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.INVALID_QUESTION_ID);
            }

            String queryType = question.getType();
            JudgeJobPayload jobPayload = new JudgeJobPayload();
            jobPayload.setJobId(RemoteServiceImpl.hashSessionId(userId, question.getDatasetId()));
            jobPayload.setType("SQL");
            jobPayload.setUserId(userId);

            List<TestCase> testCases = testCaseService.findTestCasesByQuestionId(obj.getQuestionId());
            if (testCases.isEmpty()) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.INVALID_TESTCASES);
            }

            String expectedSql = testCaseService.findExpectedSql(obj.getQuestionId());
            SQLPayload sqlPayload = new SQLPayload(obj.getQuery(), obj.getQuestionId(), queryType, expectedSql,
                    testCases, obj.getSqlMode());

            String payloadJson = objectMapper.writeValueAsString(sqlPayload);
            jobPayload.setPayload(payloadJson);

            // Push job onto the async queue — engine worker will process it.
            String jobJson = objectMapper.writeValueAsString(jobPayload);
            stringRedisTemplate.opsForList().leftPush(JUDGE_QUEUE, jobJson);

            // Store userId so JudgeService can persist history when polled.
            stringRedisTemplate.opsForValue().set(
                    META_PREFIX + jobPayload.getJobId(), userId, Duration.ofSeconds(META_TTL_S));

            SubmissionStatusResponseDTO response = new SubmissionStatusResponseDTO();
            response.setJobId(jobPayload.getJobId());
            response.setStatus("QUEUED");
            response.setMessage("Job queued for processing");

            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, response);

        } catch (Exception ex) {
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR,
                    ex);
        }
    }

    public ResponseEntity<ApiResponse<RunTestcaseResponseDTO>> runQuery(SQLInputDTO obj, String userId) {
        try {
            Question question = questionService.getByIdRaw(obj.getQuestionId());
            if (question == null) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.INVALID_QUESTION_ID);
            }

            String queryType = question.getType();
            // Generate JudgeJobPayload -> id, type, timestamp.
            JudgeJobPayload jobPayload = new JudgeJobPayload();
            jobPayload.setJobId(RemoteServiceImpl.hashSessionId(userId, question.getDatasetId()));
            jobPayload.setType("SQL");
            jobPayload.setUserId(userId);

            // Fetch and set TCs from questions ->
            List<TestCase> testCases = testCaseService.findTestCasesByTypeAndQuestionId(TestCaseType.PUBLIC,
                    obj.getQuestionId());

            if (testCases.isEmpty()) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.INVALID_TESTCASES);
            }

            String expectedSql = testCaseService.findExpectedSql(obj.getQuestionId());

            SQLPayload sqlPayload = new SQLPayload(obj.getQuery(), obj.getQuestionId(), queryType, expectedSql,
                    testCases, obj.getSqlMode());

            // payload as string from Testcases
            String payload = objectMapper.writeValueAsString(sqlPayload);
            jobPayload.setPayload(payload);

            // Call to remote judge.
            RunTestcaseResponseDTO judgeResponse = sqlRemoteRepository.runPublicTestCases(jobPayload);
            return ApiResponse.call(
                    HttpStatus.OK,
                    MessageConstants.OK,
                    judgeResponse);

        } catch (Exception ex) {
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageConstants.INTERNAL_SERVER_ERROR,
                    ex);
        }
    }

}