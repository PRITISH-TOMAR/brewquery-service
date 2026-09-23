package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class TestCaseQueries {

    private static final String SELECT = """
            SELECT id, question_id AS questionId, type,
                   expected_sql AS expectedSql, test_cases::TEXT AS testCasesJson
            FROM test_case_groups
            """;

    public final String FIND_ALL = SELECT;

    public final String FIND_BY_ID = SELECT + "WHERE id = ?";

    public final String FIND_BY_QUESTION_ID = SELECT + "WHERE question_id = ?";

    public final String FIND_EXPECTED_SQL = """
            SELECT expected_sql FROM test_case_groups WHERE question_id = ?
            """;

    public final String FIND_BY_QUESTION_IDS = SELECT + "WHERE question_id = ANY(?)";

    public final String UPSERT = """
            INSERT INTO test_case_groups (id, question_id, type, expected_sql, test_cases)
            VALUES (?, ?, ?, ?, ?::JSONB)
            ON CONFLICT (id) DO UPDATE SET
                question_id  = EXCLUDED.question_id,
                type         = EXCLUDED.type,
                expected_sql = EXCLUDED.expected_sql,
                test_cases   = EXCLUDED.test_cases
            """;

    public final String DELETE_BY_ID = "DELETE FROM test_case_groups WHERE id = ?";
}
