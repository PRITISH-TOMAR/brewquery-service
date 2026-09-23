package club.sqlhub.mongo.models;

import lombok.Data;

public class JudgeResult {
    @Data
    public static class JudgeResultDTO {
        private String jobId;
        private String userId;
        private String questionId;
        private String questionTitle;
        private String dataset;
        private String level;
        private String language;
        private java.util.Date submittedAt;
        private Object result;
    }
}
