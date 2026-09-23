package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class QuestionQueries {

    private static final String SELECT = """
            SELECT id, dataset_id AS datasetId, title, question, difficulty,
                   tags::TEXT AS tags, type, table_names::TEXT AS tableNames,
                   created_at AS createdAt
            FROM questions
            """;

    public final String FIND_BY_ID = SELECT + "WHERE id = ?";

    public final String FIND_BY_DATASET_ID = SELECT + "WHERE dataset_id = ?";

    public final String UPSERT = """
            INSERT INTO questions
                (id, dataset_id, title, question, difficulty, tags, type, table_names, created_at)
            VALUES (?, ?, ?, ?, ?, ?::JSONB, ?, ?::JSONB, ?)
            ON CONFLICT (id) DO UPDATE SET
                dataset_id  = EXCLUDED.dataset_id,
                title       = EXCLUDED.title,
                question    = EXCLUDED.question,
                difficulty  = EXCLUDED.difficulty,
                tags        = EXCLUDED.tags,
                type        = EXCLUDED.type,
                table_names = EXCLUDED.table_names
            """;

    public final String FIND_ALL_BY_IDS = SELECT + "WHERE id = ANY(?)";

    public final String DELETE_BY_ID = "DELETE FROM questions WHERE id = ?";
}
