package in.brewquery_engine.sql_engine.judge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import in.brewquery_engine.entities.query.SQLQueryResponseDTO;

@Component
public class ResultComparator {

    public boolean compare(SQLQueryResponseDTO user, SQLQueryResponseDTO expected, Double numericTolerance) {
        if (user == null || expected == null) return false;

        // Column sets must match
        Set<String> userCols = new HashSet<>(user.getColumns());
        Set<String> expectedCols = new HashSet<>(expected.getColumns());
        if (!userCols.equals(expectedCols)) return false;

        // Row count must match
        if (user.getRowsCount() != expected.getRowsCount()) return false;

        // Sort both result sets by string representation for order-insensitive comparison
        List<List<Object>> userRows = sortRows(user.getRows());
        List<List<Object>> expectedRows = sortRows(expected.getRows());

        for (int i = 0; i < userRows.size(); i++) {
            List<Object> userRow = userRows.get(i);
            List<Object> expectedRow = expectedRows.get(i);

            if (userRow.size() != expectedRow.size()) return false;

            for (int j = 0; j < userRow.size(); j++) {
                if (!valuesMatch(userRow.get(j), expectedRow.get(j), numericTolerance)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean valuesMatch(Object a, Object b, Double tolerance) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (tolerance != null && isNumeric(a) && isNumeric(b)) {
            double da = toDouble(a);
            double db = toDouble(b);
            return Math.abs(da - db) <= tolerance;
        }

        return a.toString().equals(b.toString());
    }

    private boolean isNumeric(Object val) {
        return val instanceof Number;
    }

    private double toDouble(Object val) {
        return ((Number) val).doubleValue();
    }

    private List<List<Object>> sortRows(List<List<Object>> rows) {
        List<List<Object>> copy = new ArrayList<>(rows);
        copy.sort(Comparator.comparing(row ->
                row.stream()
                   .map(v -> v == null ? "" : v.toString())
                   .reduce("", (a, b) -> a + "|" + b)
        ));
        return copy;
    }
}
