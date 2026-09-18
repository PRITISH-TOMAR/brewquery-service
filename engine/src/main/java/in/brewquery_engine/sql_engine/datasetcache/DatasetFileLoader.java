package in.brewquery_engine.sql_engine.datasetcache;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DatasetFileLoader {

    public String loadSqlFile(String basePath, String datasetId, String fileName) throws Exception {
        Path path = Paths.get(basePath, datasetId, fileName);
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public String loadJsonFile(String basePath, String datasetId, String fileName) throws Exception {
        Path path = Paths.get(basePath, datasetId, fileName);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
