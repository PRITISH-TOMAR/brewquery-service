package club.sqlhub.service.remoteService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.Repository.remoteRepository.SQLRemoteRepository;
import club.sqlhub.entity.judge.JudgeServerJobDTO.JudgeJobPayload;
import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionStatusResponseDTO;
import club.sqlhub.entity.judge.SQLDTO.SQLInputDTO;
import club.sqlhub.entity.judge.SQLDTO.SQLPayload;
import club.sqlhub.mongo.models.Question;
import club.sqlhub.mongo.models.TestCaseSQL.TestCase;
import club.sqlhub.mongo.service.QuestionService;
import club.sqlhub.mongo.service.TestCaseService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.remoteServiceHelper.RemoteServiceImpl;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SQLRemoteService {

    private final QuestionService questionService;
    private final TestCaseService testCaseService;
    private final ObjectMapper objectMapper;
    private final SQLRemoteRepository sqlRemoteRepository;

    public ResponseEntity<ApiResponse<SubmissionStatusResponseDTO>> executeQuery(
            SQLInputDTO obj,
            String userId) {

        try {
            // 1) Validate Question
            Question question = questionService.getByIdRaw(obj.getQuestionId());
            if (question == null) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, "Invalid questionId");
            }
            String queryType = question.getType();
            // Generate JudgeJobPayload -> id, type, timestamp.
            JudgeJobPayload jobPayload = new JudgeJobPayload();
            jobPayload.setJobId(RemoteServiceImpl.hashSessionId(userId, question.getDatasetId()));
            jobPayload.setType("SQL");
            jobPayload.setUserId(userId);

            // Fetch and set TCs from questions ->
            List<TestCase> testCases = testCaseService.findTestCasesByQuestionId(obj.getQuestionId());

            if (testCases == null) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, "No test cases found");
            }

            String expectedSql = testCaseService.findExpectedSql(obj.getQuestionId());

            SQLPayload sqlPayload = new SQLPayload(obj.getQuery(), obj.getQuestionId(), queryType, expectedSql,
                    testCases);

            // payload as string from Testcases
            String payload = objectMapper.writeValueAsString(sqlPayload);
            jobPayload.setPayload(payload);

            // Call to remote judge .
            SubmissionStatusResponseDTO judgeResponse = sqlRemoteRepository.submitQuery(jobPayload);
            return ApiResponse.call(
                    HttpStatus.OK,
                    "Query executed successfully",
                    judgeResponse);

        } catch (Exception ex) {
            return ApiResponse.error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal error while executing query",
                    ex);
        }
    }

}