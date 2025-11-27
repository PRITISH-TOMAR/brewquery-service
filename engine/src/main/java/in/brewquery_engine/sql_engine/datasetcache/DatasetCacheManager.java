package in.brewquery_engine.sql_engine.datasetcache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import in.brewquery_engine.constants.StorageConstants;
import in.brewquery_engine.entities.*;

@Component
public class DatasetCacheManager {

    private final DatasetFileLoader fileLoader;

    private String basePath;
    private final ObjectMapper mapper;
    private final Map<String, CachedDatasetDTO> cache;

    public DatasetCacheManager(DatasetFileLoader fileLoader) {
        this.fileLoader = fileLoader;
        this.mapper = new ObjectMapper();
        this.basePath = StorageConstants.BASE_DIR_PATH;
        this.cache = new ConcurrentHashMap<>();
    }

    public CachedDatasetDTO getDataset(String datasetId) throws Exception {

        if (cache.containsKey(datasetId)) {
            return cache.get(datasetId);
        }

        CachedDatasetDTO dto = new CachedDatasetDTO();

        dto.setSchemaStatements(fileLoader.loadSqlFile(basePath, datasetId, "schema.sql"));
        dto.setInsertStatements(fileLoader.loadSqlFile(basePath, datasetId, "inserts.sql"));

        // String qJson = fileLoader.loadJsonFile(basePath, datasetId, "questions.json");
        // Map<Integer, String> questions = mapper.readValue(qJson, new TypeReference<Map<Integer, String>>() {
        // });

        // String sJson = fileLoader.loadJsonFile(basePath, datasetId, "solutions.json");
        // Map<Integer, String> solutions = mapper.readValue(sJson, new TypeReference<Map<Integer, String>>() {
        // });

        // dto.setQuestions(questions);
        // dto.setSolutions(solutions);
        cache.put(datasetId, dto);
        return dto;

    }

}
