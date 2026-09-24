package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class DatasetQueries {

    private static final String SELECT = """
            SELECT id::TEXT AS id, slug, title, description, icon,
                   cover_image AS coverImage, er_image AS erImage,
                   tags::TEXT AS tags, categories::TEXT AS categories,
                   skills::TEXT AS skills, difficulty,
                   sql_modes_available::TEXT AS sqlModesAvailable,
                   questions, table_count AS tableCount, data_type AS dataType,
                   estimated_time AS estimatedTime, created_at AS createdAt,
                   deleted_at AS deletedAt
            FROM datasets
            """;

    public final String FIND_ALL_PAGED = SELECT +
            "WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT ? OFFSET ?";

    public final String COUNT_ALL = "SELECT COUNT(*) FROM datasets WHERE deleted_at IS NULL";

    public final String FIND_ALL_BY_TITLE_PAGED = SELECT +
            "WHERE deleted_at IS NULL AND LOWER(title) LIKE LOWER(?) ORDER BY created_at DESC LIMIT ? OFFSET ?";

    public final String COUNT_BY_TITLE =
            "SELECT COUNT(*) FROM datasets WHERE deleted_at IS NULL AND LOWER(title) LIKE LOWER(?)";

    public final String FIND_BY_ID = SELECT + "WHERE id = CAST(? AS BIGINT)";

    public final String FIND_ALL_BY_IDS = SELECT + "WHERE id = ANY(CAST(? AS BIGINT[]))";

    public final String INSERT = """
            INSERT INTO datasets
                (slug, title, description, icon, cover_image, er_image,
                 tags, categories, skills, difficulty, sql_modes_available,
                 questions, table_count, data_type, estimated_time, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?::JSONB, ?::JSONB, ?::JSONB, ?, ?::JSONB, ?, ?, ?, ?, ?)
            RETURNING id::TEXT AS id
            """;

    public final String UPDATE = """
            UPDATE datasets SET
                slug                = ?,
                title               = ?,
                description         = ?,
                icon                = ?,
                cover_image         = ?,
                er_image            = ?,
                tags                = ?::JSONB,
                categories          = ?::JSONB,
                skills              = ?::JSONB,
                difficulty          = ?,
                sql_modes_available = ?::JSONB,
                questions           = ?,
                table_count         = ?,
                data_type           = ?,
                estimated_time      = ?
            WHERE id = CAST(? AS BIGINT)
            """;

    public final String DELETE_BY_ID = "DELETE FROM datasets WHERE id = CAST(? AS BIGINT)";

    public final String UPDATE_COVER = "UPDATE datasets SET cover_image = ? WHERE id = CAST(? AS BIGINT)";

    public final String UPDATE_ER = "UPDATE datasets SET er_image = ? WHERE id = CAST(? AS BIGINT)";

    public final String SOFT_DELETE_BY_ID =
            "UPDATE datasets SET deleted_at = NOW() WHERE id = CAST(? AS BIGINT)";

    public final String INCREMENT_QUESTIONS =
            "UPDATE datasets SET questions = questions + 1 WHERE id = CAST(? AS BIGINT)";

    public final String DECREMENT_QUESTIONS =
            "UPDATE datasets SET questions = GREATEST(questions - 1, 0) WHERE id = CAST(? AS BIGINT)";
}
