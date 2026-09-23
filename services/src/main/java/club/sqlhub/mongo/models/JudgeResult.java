package club.sqlhub.mongo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

public class JudgeResult {
    @Data
    @Document(collection = "judge_results")
    public static class JudgeResultDTO {
        @Id
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
