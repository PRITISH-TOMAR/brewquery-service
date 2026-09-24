package club.sqlhub.Repository;

import java.sql.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.queries.DatasetQueries;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatasetSQLRepository {

    private final JdbcTemplate   jdbc;
    private final DatasetQueries queries;
    private final ObjectMapper   mapper;

    private RowMapper<Dataset> rowMapper() { return (rs, rn) -> {
        Dataset d = new Dataset();
        d.setId(rs.getString("id"));
        d.setSlug(rs.getString("slug"));
        d.setTitle(rs.getString("title"));
        d.setDescription(rs.getString("description"));
        d.setIcon(rs.getString("icon"));
        d.setCoverImage(rs.getString("coverImage"));
        d.setErImage(rs.getString("erImage"));
        d.setDifficulty(rs.getString("difficulty"));
        d.setQuestions(rs.getInt("questions"));
        d.setTableCount(rs.getInt("tableCount"));
        d.setDataType(rs.getString("dataType"));
        d.setEstimatedTime(rs.getString("estimatedTime"));
        try {
            d.setTags(mapper.readValue(rs.getString("tags"), new TypeReference<>() {}));
            d.setCategories(mapper.readValue(rs.getString("categories"), new TypeReference<>() {}));
            d.setSkills(mapper.readValue(rs.getString("skills"), new TypeReference<>() {}));
            d.setSqlModesAvailable(mapper.readValue(rs.getString("sqlModesAvailable"), new TypeReference<>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize dataset JSONB fields", e);
        }
        java.sql.Timestamp deletedAt = rs.getTimestamp("deletedAt");
        d.setDeletedAt(deletedAt != null ? deletedAt.toLocalDateTime() : null);
        return d;
    }; }

    public Dataset findById(String id) {
        try {
            return jdbc.queryForObject(queries.FIND_BY_ID, rowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Dataset> findAllPaged(int limit, int offset) {
        return jdbc.query(queries.FIND_ALL_PAGED, rowMapper(), limit, offset);
    }

    public long countAll() {
        Long count = jdbc.queryForObject(queries.COUNT_ALL, Long.class);
        return count != null ? count : 0L;
    }

    public List<Dataset> findByTitlePaged(String search, int limit, int offset) {
        return jdbc.query(queries.FIND_ALL_BY_TITLE_PAGED, rowMapper(), "%" + search + "%", limit, offset);
    }

    public long countByTitle(String search) {
        Long count = jdbc.queryForObject(queries.COUNT_BY_TITLE, Long.class, "%" + search + "%");
        return count != null ? count : 0L;
    }

    public Dataset save(Dataset d) {
        try {
            if (d.getId() == null) {
                String id = jdbc.queryForObject(queries.INSERT, String.class,
                        d.getSlug(), d.getTitle(), d.getDescription(),
                        d.getIcon(), d.getCoverImage(), d.getErImage(),
                        mapper.writeValueAsString(d.getTags()),
                        mapper.writeValueAsString(d.getCategories()),
                        mapper.writeValueAsString(d.getSkills()),
                        d.getDifficulty(),
                        mapper.writeValueAsString(d.getSqlModesAvailable()),
                        d.getQuestions(), d.getTableCount(), d.getDataType(),
                        d.getEstimatedTime(), d.getCreatedAt());
                d.setId(id);
            } else {
                jdbc.update(queries.UPDATE,
                        d.getSlug(), d.getTitle(), d.getDescription(),
                        d.getIcon(), d.getCoverImage(), d.getErImage(),
                        mapper.writeValueAsString(d.getTags()),
                        mapper.writeValueAsString(d.getCategories()),
                        mapper.writeValueAsString(d.getSkills()),
                        d.getDifficulty(),
                        mapper.writeValueAsString(d.getSqlModesAvailable()),
                        d.getQuestions(), d.getTableCount(), d.getDataType(),
                        d.getEstimatedTime(), d.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save dataset", e);
        }
        return d;
    }

    public void deleteById(String id) {
        jdbc.update(queries.DELETE_BY_ID, id);
    }

    public List<Dataset> findAllById(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        try {
            Array arr = jdbc.getDataSource().getConnection()
                    .createArrayOf("VARCHAR", ids.toArray(new String[0]));
            return jdbc.query(queries.FIND_ALL_BY_IDS, rowMapper(), arr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query datasets by ids", e);
        }
    }

    public void updateCoverImage(String datasetId, String url) {
        jdbc.update(queries.UPDATE_COVER, url, datasetId);
    }

    public void updateErImage(String datasetId, String url) {
        jdbc.update(queries.UPDATE_ER, url, datasetId);
    }

    public void softDeleteById(String id) {
        jdbc.update(queries.SOFT_DELETE_BY_ID, id);
    }

    public void incrementQuestions(String datasetId) {
        jdbc.update(queries.INCREMENT_QUESTIONS, datasetId);
    }

    public void decrementQuestions(String datasetId) {
        jdbc.update(queries.DECREMENT_QUESTIONS, datasetId);
    }
}
