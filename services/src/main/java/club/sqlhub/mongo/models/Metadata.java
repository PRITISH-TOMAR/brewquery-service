package club.sqlhub.mongo.models;

import java.util.List;

import lombok.Data;

@Data
public class Metadata {

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
        private boolean notNull;
        private String foreignKey;
    }
}
