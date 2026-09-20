package in.brewquery_engine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import in.brewquery_engine.entities.judge.JudgeJobPayload;
import in.brewquery_engine.entities.judge.RunTestcaseResponseDTO;
import in.brewquery_engine.entities.judge.SQLPayload;
import in.brewquery_engine.entities.judge.TestCaseRequest;
import in.brewquery_engine.entities.judge.TestCaseResult;
import in.brewquery_engine.sql_engine.judge.PayloadParser;
import in.brewquery_engine.sql_engine.judge.TestCaseExecutor;
import in.brewquery_engine.sql_engine.validator.SafeQueryValidator;
import in.brewquery_engine.utils.APiResponse.ApiResponse;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JobsService {

    private final PayloadParser payloadParser;
    private final TestCaseExecutor testCaseExecutor;

    private static final String FALLBACK_SQL_MODE = "MySQL";

    public ResponseEntity<ApiResponse<RunTestcaseResponseDTO>> runPublicTestCases(JudgeJobPayload req) {
        try {
            SQLPayload payload = payloadParser.parse(req.getPayload());

            if (!SafeQueryValidator.validateQuery(payload.getSql(), payload.getType())) {
                return ApiResponse.call(HttpStatus.BAD_REQUEST, "UNSAFE_QUERY");
            }

            List<TestCaseRequest> publicTCs = payload.getTestCases()
                    .stream()
                    .filter(tc -> "public".equalsIgnoreCase(tc.getType()))
                    .collect(Collectors.toList());

            String sqlMode = payload.getSqlMode() != null ? payload.getSqlMode() : FALLBACK_SQL_MODE;
            long startMs = System.currentTimeMillis();
            List<TestCaseResult> results = executeAll(publicTCs, payload.getSql(), payload.getExpectedSql(), sqlMode);
            long totalMs = System.currentTimeMillis() - startMs;

            RunTestcaseResponseDTO response = buildRunResponse(results, totalMs);
            return ApiResponse.call(HttpStatus.OK, "OK", response);

        } catch (Exception ex) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex);
        }
    }

    /** Called by JobWorker (async) and the HTTP endpoint (direct). */
    public RunTestcaseResponseDTO processSubmission(JudgeJobPayload req) throws Exception {
        SQLPayload payload = payloadParser.parse(req.getPayload());

        if (!SafeQueryValidator.validateQuery(payload.getSql(), payload.getType())) {
            RunTestcaseResponseDTO err = new RunTestcaseResponseDTO();
            err.setOverallStatus("UNSAFE_QUERY");
            err.setPassedCount(0);
            err.setTotalCount(payload.getTestCases() != null ? payload.getTestCases().size() : 0);
            return err;
        }

        String sqlMode = payload.getSqlMode() != null ? payload.getSqlMode() : FALLBACK_SQL_MODE;
        long startMs = System.currentTimeMillis();
        List<TestCaseResult> results = executeAll(
                payload.getTestCases(), payload.getSql(), payload.getExpectedSql(), sqlMode);
        long totalMs = System.currentTimeMillis() - startMs;

        return buildRunResponse(results, totalMs);
    }

    public ResponseEntity<ApiResponse<RunTestcaseResponseDTO>> submitQuery(JudgeJobPayload req) {
        try {
            return ApiResponse.call(HttpStatus.OK, "OK", processSubmission(req));
        } catch (Exception ex) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex);
        }
    }

    private List<TestCaseResult> executeAll(List<TestCaseRequest> testCases, String userSql, String expectedSql, String sqlMode) {
        List<TestCaseResult> results = new ArrayList<>();
        for (TestCaseRequest tc : testCases) {
            results.add(testCaseExecutor.execute(tc, userSql, expectedSql, sqlMode));
        }
        return results;
    }

    private RunTestcaseResponseDTO buildRunResponse(List<TestCaseResult> results, long totalMs) {
        int passed = (int) results.stream().filter(TestCaseResult::isPassed).count();
        int total = results.size();

        String overallStatus;
        if (passed == total) {
            overallStatus = "PASS";
        } else if (passed == 0) {
            overallStatus = "FAIL";
        } else {
            overallStatus = "PARTIAL";
        }

        RunTestcaseResponseDTO response = new RunTestcaseResponseDTO();
        response.setPassedCount(passed);
        response.setTotalCount(total);
        response.setTotalExecutionMs(totalMs);
        response.setOverallStatus(overallStatus);
        response.setTestDetails(results);
        return response;
    }
}
