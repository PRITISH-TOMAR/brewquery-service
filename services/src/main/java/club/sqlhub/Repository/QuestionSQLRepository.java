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

import club.sqlhub.mongo.models.Question;
import club.sqlhub.queries.QuestionQueries;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuestionSQLRepository {

    private final JdbcTemplate    jdbc;
    private final QuestionQueries queries;
    private final ObjectMapper    mapper;

    private RowMapper<Question> rowMapper() { return (rs, rn) -> {
        Question q = new Question();
        q.setId(rs.getString("id"));
        q.setDatasetId(rs.getString("datasetId"));
        q.setTitle(rs.getString("title"));
        q.setQuestion(rs.getString("question"));
        q.setDifficulty(rs.getString("difficulty"));
        q.setType(rs.getString("type"));
        try {
            q.setTags(mapper.readValue(rs.getString("tags"), new TypeReference<>() {}));
            q.setTableNames(mapper.readValue(rs.getString("tableNames"), new TypeReference<>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize question JSONB fields", e);
        }
        java.sql.Timestamp deletedAt = rs.getTimestamp("deletedAt");
        q.setDeletedAt(deletedAt != null ? deletedAt.toLocalDateTime() : null);
        return q;
    }; }

    public Question findById(String id) {
        try {
            return jdbc.queryForObject(queries.FIND_BY_ID, rowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Question> findByDatasetId(String datasetId) {
        return jdbc.query(queries.FIND_BY_DATASET_ID, rowMapper(), datasetId);
    }

    public Question save(Question q) {
        try {
            if (q.getId() == null) {
                String id = jdbc.queryForObject(queries.INSERT, String.class,
                        q.getDatasetId(), q.getTitle(), q.getQuestion(),
                        q.getDifficulty(),
                        mapper.writeValueAsString(q.getTags()),
                        q.getType(),
                        mapper.writeValueAsString(q.getTableNames()),
                        q.getCreatedAt());
                q.setId(id);
            } else {
                jdbc.update(queries.UPDATE,
                        q.getDatasetId(), q.getTitle(), q.getQuestion(),
                        q.getDifficulty(),
                        mapper.writeValueAsString(q.getTags()),
                        q.getType(),
                        mapper.writeValueAsString(q.getTableNames()),
                        q.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save question", e);
        }
        return q;
    }

    public List<Question> findAllById(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        try {
            Array arr = jdbc.getDataSource().getConnection()
                    .createArrayOf("VARCHAR", ids.toArray(new String[0]));
            return jdbc.query(queries.FIND_ALL_BY_IDS, rowMapper(), arr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query questions by ids", e);
        }
    }

    public void deleteById(String id) {
        jdbc.update(queries.DELETE_BY_ID, id);
    }

    public void softDeleteById(String id) {
        jdbc.update(queries.SOFT_DELETE_BY_ID, id);
    }
}
