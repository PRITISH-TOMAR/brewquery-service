package in.brewquery_engine.entities.judge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCaseRequest {
    private String id;
    private String schemaSql;
    private String seedSql;
    private Double numericTolerance;
    private String type;
}
