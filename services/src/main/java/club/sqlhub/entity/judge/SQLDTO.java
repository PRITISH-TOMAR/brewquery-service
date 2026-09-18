package club.sqlhub.entity.judge;

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
}