package club.sqlhub.entity.judge;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

public class SQLDTO {

    @Data
    public static class SQLInputDTO {
        private String questionId;
        private String query;
        private String sqlMode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseEnginePayload {
        private String id;
        private String schemaSql;
        private String seedSql;
        private Double numericTolerance;
        private String type;
    }

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class SQLPayload {
        private String sql;
        private String questionId;
        private String type;
        private String expectedSql;
        private List<TestCaseEnginePayload> testCases;
        private String sqlMode;
    }
}