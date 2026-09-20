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
        private Object result;
    }
}
