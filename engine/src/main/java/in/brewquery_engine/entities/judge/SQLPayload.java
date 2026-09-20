package in.brewquery_engine.entities.judge;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SQLPayload {
    private String sql;
    private String questionId;
    private String type;
    private String expectedSql;
    private List<TestCaseRequest> testCases;
    private String sqlMode;
}
