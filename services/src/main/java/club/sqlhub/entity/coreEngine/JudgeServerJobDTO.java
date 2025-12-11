package club.sqlhub.entity.coreEngine;

import java.security.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class JudgeServerJobDTO {

    // For every job query
    @Data
    public static class JudgeJobPayload {
        private String jobId;
        private String type;
        private String payload;
        private Long timestamp;
        private String userId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EngineQueryRequestDTO {
        private String jobId;
        private String userId;
        private String timestamp;
        private String type; // SQL, C , C++, Java
        private String payload;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubmissionStatusResponseDTO {
        private String jobId;
        private String status;
        private String message;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompareQueryDTO {
        private List<String> columns;
        private List<List<Object>> rows;
        private Integer rowsCount;
    }

    @Data
    public static class ExpectedOutputRequestDTO {
        private String sessionId;
        private String datasetId;
        private String questionId;
        private String sqlMode;
    }

    @Data
    public static class OutputDataDTO {
        private List<String> columns;
        private List<Map<String, Object>> rows;
        private Integer rowsCount;
    }

    @Data
    public class TestcaseOutputDTO {
        private String testcaseId;
        private String status; // PASS | FAIL | ERROR
        private OutputDataDTO actual;
        private OutputDataDTO expected;
        private String errorMessage;
        private Long executionTimeMs;
        private String resultHash; // optional: hash of expected
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class JudgeResponseDTO {
        private String jobId;
        private String status; // COMPLETED | FAILED
        private Integer passed;
        private Integer total;
        private Long totalExecutionTimeMs;
        private List<TestcaseOutputDTO> detailedResults;
    }

}
