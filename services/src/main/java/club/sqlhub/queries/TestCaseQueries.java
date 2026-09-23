package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class TestCaseQueries {

    private static final String SELECT = """
            SELECT id::TEXT AS id, question_id::TEXT AS questionId, type,
                   expected_sql AS expectedSql, test_cases::TEXT AS testCasesJson
            FROM test_case_groups
            """;

    public final String FIND_ALL = SELECT;

    public final String FIND_BY_ID = SELECT + "WHERE id = CAST(? AS BIGINT)";

    public final String FIND_BY_QUESTION_ID = SELECT + "WHERE question_id = CAST(? AS BIGINT)";

    public final String FIND_EXPECTED_SQL = """
            SELECT expected_sql FROM test_case_groups WHERE question_id = CAST(? AS BIGINT)
            """;

    public final String FIND_BY_QUESTION_IDS = SELECT + "WHERE question_id = ANY(CAST(? AS BIGINT[]))";

    public final String INSERT = """
            INSERT INTO test_case_groups (question_id, type, expected_sql, test_cases)
            VALUES (CAST(? AS BIGINT), ?, ?, ?::JSONB)
            RETURNING id::TEXT AS id
            """;

    public final String UPDATE = """
            UPDATE test_case_groups SET
                type         = ?,
                expected_sql = ?,
                test_cases   = ?::JSONB
            WHERE id = CAST(? AS BIGINT)
            """;

    public final String DELETE_BY_ID = "DELETE FROM test_case_groups WHERE id = CAST(? AS BIGINT)";
}
