package in.brewquery_engine.sql_engine.loader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import in.brewquery_engine.constants.StorageConstants;

@Component
public class DatasetLoadValidator {
    private final String basePath = StorageConstants.BASE_DIR_PATH;

    public void validate(String datasetId) {
        Path path = Paths.get(basePath, datasetId);

        if (!Files.exists(path)) {
            throw new RuntimeException("Dataset dir not found" + datasetId);
        }

        check(path, "schema.sql");
        check(path, "inserts.sql");
    }

    private void check(Path basePath, String file) {
        if (!Files.exists(basePath.resolve(file))) {
            throw new RuntimeException("Missing dataset file: " + file);
        }
    }
}
