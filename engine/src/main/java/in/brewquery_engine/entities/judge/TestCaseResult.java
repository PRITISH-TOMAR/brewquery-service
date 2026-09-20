package in.brewquery_engine.entities.judge;

import in.brewquery_engine.entities.query.SQLQueryResponseDTO;
import lombok.Data;

@Data
public class TestCaseResult {
    private String testCaseId;
    private boolean passed;
    private SQLQueryResponseDTO userOutput;
    private SQLQueryResponseDTO expectedOutput;
    private String error;
}
