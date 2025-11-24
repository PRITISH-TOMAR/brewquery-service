package in.brewquery_engine.sql_engine.datasetcache;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DatasetFileLoader {

    public List<String> loadSqlFile(String basePath, String datasetId, String fileName) throws Exception {

        Path path = Paths.get(basePath, datasetId, fileName);

        String content = Files.readString(path, StandardCharsets.UTF_8);

        return Arrays.stream(content.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s + ";")
                .collect(Collectors.toList());
    }

    public String loadJsonFile(String basePath, String datasetId, String fileName) throws Exception {
        Path path = Paths.get(basePath, datasetId, fileName);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
