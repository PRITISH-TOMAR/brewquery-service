package in.brewquery_engine.entities;

import lombok.Data;

@Data
public class CachedDatasetDTO {
    private String schemaStatements;
    private String insertStatements;
    private String resetStatements;
}
