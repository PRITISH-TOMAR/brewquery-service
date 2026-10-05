package club.sqlhub.entity.admin.request;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminTestCaseGenerateRequestDTO {

    /** PUBLIC or PRIVATE */
    private String type = "public";

    /** Optional numeric tolerance for floating-point comparisons */
    private Double numericTolerance;

    /**
     * Required only when no TC group exists yet — the correct SQL answer for this question.
     * Ignored if a group already exists (its stored expectedSql is used).
     */
    private String expectedSql;

    /** Optional group-level type label (e.g. DQL). Falls back to problem.type if omitted. */
    private String groupType;

    /**
     * Seed data for this test case.
     * Shape: [ { "table": "...", "columns": ["col1",...], "rows": [[v1,...], ...] } ]
     * Converted to INSERT SQL via SeedGenerator and sent to the judge server.
     */
    private List<Map<String, Object>> sampleData;
}
