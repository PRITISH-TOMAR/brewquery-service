package in.brewquery_engine.entities;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class CachedDatasetDTO {
    private List<String> schemaStatements;
    private List<String> insertStatements;

    private Map<Integer, String> questions;
    private Map<Integer, String> solutions;
}
