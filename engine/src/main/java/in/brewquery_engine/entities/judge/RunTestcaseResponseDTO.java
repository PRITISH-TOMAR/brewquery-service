package in.brewquery_engine.entities.judge;

import java.util.List;

import lombok.Data;

@Data
public class RunTestcaseResponseDTO {
    private int passedCount;
    private int totalCount;
    private long totalExecutionMs;
    private String overallStatus;
    private List<TestCaseResult> testDetails;
}
