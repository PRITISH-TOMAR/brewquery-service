package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class DatasetQueries {

    private static final String SELECT = """
            SELECT id, slug, title, description, icon,
                   cover_image AS coverImage, er_image AS erImage,
                   tags::TEXT AS tags, categories::TEXT AS categories,
                   skills::TEXT AS skills, difficulty,
                   sql_modes_available::TEXT AS sqlModesAvailable,
                   questions, table_count AS tableCount, data_type AS dataType,
                   estimated_time AS estimatedTime, created_at AS createdAt
            FROM datasets
            """;

    public final String FIND_ALL_PAGED = SELECT + "ORDER BY created_at DESC LIMIT ? OFFSET ?";

    public final String COUNT_ALL = "SELECT COUNT(*) FROM datasets";

    public final String FIND_ALL_BY_TITLE_PAGED = SELECT +
            "WHERE LOWER(title) LIKE LOWER(?) ORDER BY created_at DESC LIMIT ? OFFSET ?";

    public final String COUNT_BY_TITLE = "SELECT COUNT(*) FROM datasets WHERE LOWER(title) LIKE LOWER(?)";

    public final String FIND_BY_ID = SELECT + "WHERE id = ?";

    public final String UPSERT = """
            INSERT INTO datasets
                (id, slug, title, description, icon, cover_image, er_image,
                 tags, categories, skills, difficulty, sql_modes_available,
                 questions, table_count, data_type, estimated_time, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?::JSONB, ?::JSONB, ?::JSONB, ?, ?::JSONB, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                slug                = EXCLUDED.slug,
                title               = EXCLUDED.title,
                description         = EXCLUDED.description,
                icon                = EXCLUDED.icon,
                cover_image         = EXCLUDED.cover_image,
                er_image            = EXCLUDED.er_image,
                tags                = EXCLUDED.tags,
                categories          = EXCLUDED.categories,
                skills              = EXCLUDED.skills,
                difficulty          = EXCLUDED.difficulty,
                sql_modes_available = EXCLUDED.sql_modes_available,
                questions           = EXCLUDED.questions,
                table_count         = EXCLUDED.table_count,
                data_type           = EXCLUDED.data_type,
                estimated_time      = EXCLUDED.estimated_time
            """;

    public final String FIND_ALL_BY_IDS = SELECT + "WHERE id = ANY(?)";

    public final String DELETE_BY_ID = "DELETE FROM datasets WHERE id = ?";

    public final String UPDATE_COVER = "UPDATE datasets SET cover_image = ? WHERE id = ?";

    public final String UPDATE_ER = "UPDATE datasets SET er_image = ? WHERE id = ?";
}
