package club.sqlhub.entity.coreEngine;

import java.util.List;

import org.springframework.http.HttpStatusCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ExecuteQueryDTO {
    @Data
    public static class QueryRequestInputDTO {
        private String sessionId;
        private String query;
        private String questionId;
        private String sqlMode;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EngineQueryRequestDTO {
        private String sessionId;
        private String query;
        private String type; // SELECT, INSERT, UPDATE
    }

    @Data
    public static class EngineQueryResponseDTO {
        private List<String> columns;
        private List<List<Object>> rows;
        private Integer rowsCount;
        private Integer status;
        private String message;
        private boolean valid;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompareQueryDTO {
        private List<String> columns;
        private List<List<Object>> rows;
        private Integer rowsCount;
    }

}
