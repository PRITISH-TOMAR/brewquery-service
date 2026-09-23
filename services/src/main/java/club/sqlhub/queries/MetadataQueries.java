package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class MetadataQueries {

    public final String FIND_BY_ID = """
            SELECT id, tables::TEXT AS tables FROM metadata WHERE id = ?
            """;

    public final String UPSERT = """
            INSERT INTO metadata (id, tables) VALUES (?, ?::JSONB)
            ON CONFLICT (id) DO UPDATE SET tables = EXCLUDED.tables
            """;

    public final String DELETE_BY_ID = "DELETE FROM metadata WHERE id = ?";
}
