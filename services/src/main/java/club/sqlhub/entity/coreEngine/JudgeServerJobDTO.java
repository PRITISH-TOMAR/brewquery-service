package club.sqlhub.entity.coreEngine;


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
}
