package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class ExpectedSolutionQueries {

    private static final String SELECT = """
            SELECT id, question_id AS questionId, dataset_id AS datasetId,
                   sql_mode AS sqlMode, solutions::TEXT AS solutionsJson,
                   created_at AS createdAt
            FROM expected_solutions
            """;

    public final String FIND_BY_ID = SELECT + "WHERE id = ?";

    public final String FIND_BY_QUESTION_ID = SELECT + "WHERE question_id = ?";

    public final String FIND_BY_DATASET_ID = SELECT + "WHERE dataset_id = ?";

    public final String FIND_BY_QUESTION_AND_MODE = SELECT + "WHERE question_id = ? AND sql_mode = ?";

    public final String UPSERT = """
            INSERT INTO expected_solutions
                (id, question_id, dataset_id, sql_mode, solutions, created_at)
            VALUES (?, ?, ?, ?, ?::JSONB, ?)
            ON CONFLICT (id) DO UPDATE SET
                question_id = EXCLUDED.question_id,
                dataset_id  = EXCLUDED.dataset_id,
                sql_mode    = EXCLUDED.sql_mode,
                solutions   = EXCLUDED.solutions
            """;

    public final String DELETE_BY_ID = "DELETE FROM expected_solutions WHERE id = ?";
}
