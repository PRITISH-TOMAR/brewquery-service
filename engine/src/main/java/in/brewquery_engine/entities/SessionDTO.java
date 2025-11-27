package in.brewquery_engine.entities;

import lombok.Data;

@Data
public class SessionDTO {
    private String sessionId;
    private String datasetId;
    private String sqlMode;
}
