package club.sqlhub.utils.sql;

import java.util.List;
import java.util.stream.Collectors;

import club.sqlhub.mongo.models.Metadata;
import club.sqlhub.mongo.models.Metadata.ColumnSchema;
import club.sqlhub.mongo.models.Metadata.TableSchema;

public class SchemaGenerator {

    public static String generate(Metadata metadata, String sqlMode) {
        if (metadata == null || metadata.getTables() == null) return "";

        return metadata.getTables().stream()
                .map(t -> generateTable(t, sqlMode))
                .collect(Collectors.joining("\n"));
    }

    private static String generateTable(TableSchema table, String sqlMode) {
        List<ColumnSchema> cols = table.getColumns();

        List<String> pkCols = cols.stream()
                .filter(ColumnSchema::isPrimary)
                .map(ColumnSchema::getName)
                .collect(Collectors.toList());
        boolean compositeKey = pkCols.size() > 1;

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(table.getName()).append(" (\n");

        for (int i = 0; i < cols.size(); i++) {
            ColumnSchema col = cols.get(i);
            sb.append("  ").append(col.getName()).append(" ").append(col.getType());

            if (col.isPrimary() && !compositeKey) {
                sb.append(" PRIMARY KEY");
            }
            if (col.isNotNull() && !col.isPrimary()) {
                sb.append(" NOT NULL");
            }
            if (col.getForeignKey() != null && !col.getForeignKey().isEmpty()) {
                String[] parts = col.getForeignKey().split("\\.");
                if (parts.length == 2) {
                    sb.append(" REFERENCES ").append(parts[0]).append("(").append(parts[1]).append(")");
                }
            }

            boolean isLast = (i == cols.size() - 1) && !compositeKey;
            sb.append(isLast ? "\n" : ",\n");
        }

        if (compositeKey) {
            sb.append("  PRIMARY KEY (")
              .append(String.join(", ", pkCols))
              .append(")\n");
        }

        sb.append(");");
        return sb.toString();
    }
}
