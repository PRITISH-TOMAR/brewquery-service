package in.brewquery_engine.entities.query;

import lombok.Data;

@Data
public class SQLQueryRequestDTO {
    private String sessionId;
    private String query;
    private String type;
}
