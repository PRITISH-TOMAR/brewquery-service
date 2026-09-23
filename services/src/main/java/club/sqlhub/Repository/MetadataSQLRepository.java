package club.sqlhub.Repository;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.mongo.models.Metadata;
import club.sqlhub.queries.MetadataQueries;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MetadataSQLRepository {

    private final JdbcTemplate    jdbc;
    private final MetadataQueries queries;
    private final ObjectMapper    mapper;

    public Metadata findById(String datasetId) {
        try {
            return jdbc.queryForObject(queries.FIND_BY_ID, (rs, rn) -> {
                Metadata m = new Metadata();
                m.setId(rs.getString("id"));
                try {
                    m.setTables(mapper.readValue(rs.getString("tables"),
                            new TypeReference<List<Metadata.TableSchema>>() {}));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize metadata tables", e);
                }
                return m;
            }, datasetId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Metadata save(Metadata m) {
        try {
            jdbc.update(queries.UPSERT, m.getId(), mapper.writeValueAsString(m.getTables()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save metadata", e);
        }
        return m;
    }

    public void deleteById(String id) {
        jdbc.update(queries.DELETE_BY_ID, id);
    }
}
