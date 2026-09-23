package club.sqlhub.mongo.models;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ExpectedSolution {

    private String id;

    private String questionId;
    private String datasetId;
    private String sqlMode;

    private List<SolutionEntry> solutions;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    public static class SolutionEntry {
        private String solutionQuery;
        private String resultHash;
        private OutputData expectedOutput;
    }

    @Data
    public static class OutputData {
        private List<String> columns;
        private List<List<Object>> rows;
        private Integer rowsCount;
    }
}
