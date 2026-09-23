package club.sqlhub.Repository;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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

    private RowMapper<Metadata> rowMapper() { return (rs, rn) -> {
        Metadata m = new Metadata();
        m.setId(rs.getString("id"));
        m.setDatasetId(rs.getString("datasetId"));
        try {
            m.setTables(mapper.readValue(rs.getString("tables"),
                    new TypeReference<List<Metadata.TableSchema>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize metadata tables", e);
        }
        return m;
    }; }

    public Metadata findByDatasetId(String datasetId) {
        try {
            return jdbc.queryForObject(queries.FIND_BY_DATASET_ID, rowMapper(), datasetId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Metadata save(Metadata m) {
        try {
            if (m.getId() == null) {
                String id = jdbc.queryForObject(queries.INSERT, String.class,
                        m.getDatasetId(), mapper.writeValueAsString(m.getTables()));
                m.setId(id);
            } else {
                jdbc.update(queries.UPDATE,
                        mapper.writeValueAsString(m.getTables()), m.getDatasetId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save metadata", e);
        }
        return m;
    }

    public void deleteByDatasetId(String datasetId) {
        jdbc.update(queries.DELETE_BY_DATASET_ID, datasetId);
    }
}
