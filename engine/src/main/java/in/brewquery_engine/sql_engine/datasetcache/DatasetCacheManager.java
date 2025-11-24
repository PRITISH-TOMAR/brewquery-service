package in.brewquery_engine.sql_engine.datasetcache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import in.brewquery_engine.constants.StorageConstants;
import in.brewquery_engine.entities.*;

@Component
@AllArgsConstructor
public class DatasetCacheManager {

    private final DatasetFileLoader fileLoader;
    private final ObjectMapper mapper = new ObjectMapper();
    private String basePath = StorageConstants.BasePath;

    private final Map<String, CachedDatasetDTO> cache = new ConcurrentHashMap<>();

    public CachedDatasetDTO getDataset(String datasetId) throws Exception {

        if (cache.containsKey(datasetId)) {
            return cache.get(datasetId);
        }

        CachedDatasetDTO dto = new CachedDatasetDTO();

        dto.setSchemaStatements(fileLoader.loadSqlFile(basePath, datasetId, "schema.sql"));
        dto.setSchemaStatements(fileLoader.loadSqlFile(basePath, datasetId, "inserts.sql"));

        String qJson = fileLoader.loadJsonFile(basePath, datasetId, "questions.json");
        Map<Integer, String> questions = mapper.readValue(qJson, new TypeReference<Map<Integer, String>>() {
        });

        String sJson = fileLoader.loadJsonFile(basePath, datasetId, "solutions.json");
        Map<Integer, String> solutions = mapper.readValue(sJson, new TypeReference<Map<Integer, String>>() {
        });

        dto.setQuestions(questions);
        dto.setSolutions(solutions);
        return dto;

    }

}
