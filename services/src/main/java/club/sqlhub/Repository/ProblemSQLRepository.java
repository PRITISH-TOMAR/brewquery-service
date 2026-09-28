package club.sqlhub.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.mongo.models.Problem;
import club.sqlhub.queries.ProblemQueries;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProblemSQLRepository {

    private final JdbcTemplate   jdbc;
    private final ProblemQueries queries;
    private final ObjectMapper   mapper;

    private RowMapper<Problem> rowMapper() { return (rs, rn) -> {
        Problem p = new Problem();
        p.setId(rs.getString("id"));
        p.setDatasetId(rs.getString("datasetId"));
        p.setTitle(rs.getString("title"));
        p.setQuestion(rs.getString("question"));
        p.setDifficulty(rs.getString("difficulty"));
        p.setType(rs.getString("type"));
        try {
            p.setTags(mapper.readValue(rs.getString("tags"), new TypeReference<>() {}));
            p.setTableNames(mapper.readValue(rs.getString("tableNames"), new TypeReference<>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize problem JSONB fields", e);
        }
        java.sql.Timestamp deletedAt = rs.getTimestamp("deletedAt");
        p.setDeletedAt(deletedAt != null ? deletedAt.toLocalDateTime() : null);
        return p;
    }; }

    public Problem findById(String id) {
        try {
            return jdbc.queryForObject(queries.FIND_BY_ID, rowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Problem> findByDatasetId(String datasetId) {
        return jdbc.query(queries.FIND_BY_DATASET_ID, rowMapper(), datasetId);
    }

    public Problem save(Problem p) {
        try {
            if (p.getId() == null) {
                String id = jdbc.queryForObject(queries.INSERT, String.class,
                        p.getDatasetId(), p.getTitle(), p.getQuestion(),
                        p.getDifficulty(),
                        mapper.writeValueAsString(p.getTags()),
                        p.getType(),
                        mapper.writeValueAsString(p.getTableNames()),
                        p.getCreatedAt());
                p.setId(id);
            } else {
                jdbc.update(queries.UPDATE,
                        p.getDatasetId(), p.getTitle(), p.getQuestion(),
                        p.getDifficulty(),
                        mapper.writeValueAsString(p.getTags()),
                        p.getType(),
                        mapper.writeValueAsString(p.getTableNames()),
                        p.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save problem", e);
        }
        return p;
    }

    public List<Problem> findAllById(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        String[] idArray = ids.toArray(new String[0]);
        try {
            return jdbc.query(
                conn -> {
                    java.sql.PreparedStatement ps = conn.prepareStatement(queries.FIND_ALL_BY_IDS);
                    ps.setArray(1, conn.createArrayOf("VARCHAR", idArray));
                    return ps;
                },
                rowMapper()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to query problems by ids", e);
        }
    }

    public void deleteById(String id) {
        jdbc.update(queries.DELETE_BY_ID, id);
    }

    public void softDeleteById(String id) {
        jdbc.update(queries.SOFT_DELETE_BY_ID, id);
    }
}
