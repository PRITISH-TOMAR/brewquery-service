package club.sqlhub.entity.judge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class JudgeServerJobDTO {

    // For every job query
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
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
    public static class SubmissionStatusResponseDTO {
        private String jobId;
        private String status;
        private String message;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubmissionResponseDTO {
        private String userId;
        private String executionTime;
        private String type;
        private String questionId;
        private String SubmissionTime;
        private String verdict;
        private int passCount;
        private int totalCount;
        private Object testDetails;
    }
}
