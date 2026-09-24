package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class QuestionQueries {

    private static final String SELECT = """
            SELECT id::TEXT AS id, dataset_id::TEXT AS datasetId, title, question, difficulty,
                   tags::TEXT AS tags, type, table_names::TEXT AS tableNames,
                   created_at AS createdAt, deleted_at AS deletedAt
            FROM questions
            """;

    public final String FIND_BY_ID = SELECT + "WHERE id = CAST(? AS BIGINT)";

    public final String FIND_BY_DATASET_ID = SELECT +
            "WHERE dataset_id = CAST(? AS BIGINT) AND deleted_at IS NULL";

    public final String FIND_ALL_BY_IDS = SELECT + "WHERE id = ANY(CAST(? AS BIGINT[]))";

    public final String INSERT = """
            INSERT INTO questions
                (dataset_id, title, question, difficulty, tags, type, table_names, created_at)
            VALUES (CAST(? AS BIGINT), ?, ?, ?, ?::JSONB, ?, ?::JSONB, ?)
            RETURNING id::TEXT AS id
            """;

    public final String UPDATE = """
            UPDATE questions SET
                dataset_id  = CAST(? AS BIGINT),
                title       = ?,
                question    = ?,
                difficulty  = ?,
                tags        = ?::JSONB,
                type        = ?,
                table_names = ?::JSONB
            WHERE id = CAST(? AS BIGINT)
            """;

    public final String DELETE_BY_ID = "DELETE FROM questions WHERE id = CAST(? AS BIGINT)";

    public final String SOFT_DELETE_BY_ID =
            "UPDATE questions SET deleted_at = NOW() WHERE id = CAST(? AS BIGINT)";
}
