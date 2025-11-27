package club.sqlhub.mongo.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "metadata")
@Data
public class Metadata {

    @Id
    private String id;

    private List<TableSchema> tables;

    @Data
    public static class TableSchema {
        private String name;
        private List<ColumnSchema> columns;
    }

    @Data
    public static class ColumnSchema {
        private String name;
        private String type;
        private boolean primary;
        private String foreignKey; 
    }
}
