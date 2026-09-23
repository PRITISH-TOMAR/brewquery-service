package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class ExpectedSolutionQueries {

    private static final String SELECT = """
            SELECT id::TEXT AS id, question_id::TEXT AS questionId, dataset_id::TEXT AS datasetId,
                   sql_mode AS sqlMode, solutions::TEXT AS solutionsJson,
                   created_at AS createdAt
            FROM expected_solutions
            """;

    public final String FIND_BY_ID = SELECT + "WHERE id = CAST(? AS BIGINT)";

    public final String FIND_BY_QUESTION_ID = SELECT + "WHERE question_id = CAST(? AS BIGINT)";

    public final String FIND_BY_DATASET_ID = SELECT + "WHERE dataset_id = CAST(? AS BIGINT)";

    public final String FIND_BY_QUESTION_AND_MODE = SELECT + "WHERE question_id = CAST(? AS BIGINT) AND sql_mode = ?";

    public final String INSERT = """
            INSERT INTO expected_solutions (question_id, dataset_id, sql_mode, solutions, created_at)
            VALUES (CAST(? AS BIGINT), CAST(? AS BIGINT), ?, ?::JSONB, ?)
            RETURNING id::TEXT AS id
            """;

    public final String UPDATE = """
            UPDATE expected_solutions SET
                question_id = CAST(? AS BIGINT),
                dataset_id  = CAST(? AS BIGINT),
                sql_mode    = ?,
                solutions   = ?::JSONB
            WHERE id = CAST(? AS BIGINT)
            """;

    public final String DELETE_BY_ID = "DELETE FROM expected_solutions WHERE id = CAST(? AS BIGINT)";
}
