package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class MetadataQueries {

    private static final String SELECT = """
            SELECT id::TEXT AS id, dataset_id::TEXT AS datasetId, tables::TEXT AS tables
            FROM metadata
            """;

    public final String FIND_BY_DATASET_ID = SELECT + "WHERE dataset_id = CAST(? AS BIGINT)";

    public final String INSERT = """
            INSERT INTO metadata (dataset_id, tables)
            VALUES (CAST(? AS BIGINT), ?::JSONB)
            RETURNING id::TEXT AS id
            """;

    public final String UPDATE = """
            UPDATE metadata SET tables = ?::JSONB
            WHERE dataset_id = CAST(? AS BIGINT)
            """;

    public final String DELETE_BY_DATASET_ID = "DELETE FROM metadata WHERE dataset_id = CAST(? AS BIGINT)";
}
