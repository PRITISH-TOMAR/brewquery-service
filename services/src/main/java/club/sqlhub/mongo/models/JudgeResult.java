package club.sqlhub.mongo.models;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

public class JudgeResult {
    @Data
    @Document(collection = "judge_results")
    public static class JudgeResultDTO {
        private String userId;
        private String jobId;
        private Object result;
    }
}
