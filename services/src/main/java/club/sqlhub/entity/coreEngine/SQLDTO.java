package club.sqlhub.entity.coreEngine;

import java.time.LocalDateTime;
import java.util.List;

import club.sqlhub.mongo.models.TestCaseSQL.TestCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public class SQLDTO {

    @Data
    public static class SQLInputDTO {
        private String questionId;
        private String query;
    }

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class SQLPayload {
        private String sql;
        private String questionId;
        private String type;
        private String expectedSql;
        private List<TestCase> testCases;
    }

    @Data
    public static class LoadDatasetInputDTO {
        private String sessionId;
        private String datasetId;
        private String sqlMode;
    }

    @Data
    public static class LoadDatasetOutputDTO {
        private String sessionId;
        private String datasetId;
        private String sqlMode;
        private LocalDateTime lastAccessTime;
        private String message;
        private Integer status;
    }
}
