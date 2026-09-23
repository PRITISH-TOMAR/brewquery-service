package club.sqlhub.utils.sql;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SeedGenerator {

    @SuppressWarnings("unchecked")
    public static String generate(Object sampleData) {
        if (!(sampleData instanceof List)) return "";

        StringBuilder sb = new StringBuilder();
        for (Object entry : (List<?>) sampleData) {
            if (!(entry instanceof Map)) continue;
            Map<String, Object> tableData = (Map<String, Object>) entry;

            String tableName = (String) tableData.get("table");
            List<String> columns = (List<String>) tableData.get("columns");
            List<List<Object>> rows = (List<List<Object>>) tableData.get("rows");

            if (tableName == null || columns == null || rows == null || rows.isEmpty()) continue;

            String colList = String.join(", ", columns);
            for (List<Object> row : rows) {
                String values = row.stream()
                        .map(SeedGenerator::toSqlValue)
                        .collect(Collectors.joining(", "));
                sb.append("INSERT INTO ").append(tableName)
                  .append(" (").append(colList).append(")")
                  .append(" VALUES (").append(values).append(");\n");
            }
        }
        return sb.toString().trim();
    }

    private static String toSqlValue(Object val) {
        if (val == null) return "NULL";
        if (val instanceof Boolean) return ((Boolean) val) ? "TRUE" : "FALSE";
        if (val instanceof Number) return val.toString();
        return "'" + val.toString().replace("'", "''") + "'";
    }
}
