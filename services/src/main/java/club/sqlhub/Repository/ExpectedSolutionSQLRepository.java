package club.sqlhub.Repository;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.mongo.models.ExpectedSolution;
import club.sqlhub.queries.ExpectedSolutionQueries;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExpectedSolutionSQLRepository {

    private final JdbcTemplate            jdbc;
    private final ExpectedSolutionQueries queries;
    private final ObjectMapper            mapper;

    private RowMapper<ExpectedSolution> rowMapper() { return (rs, rn) -> {
        ExpectedSolution s = new ExpectedSolution();
        s.setId(rs.getString("id"));
        s.setQuestionId(rs.getString("questionId"));
        s.setDatasetId(rs.getString("datasetId"));
        s.setSqlMode(rs.getString("sqlMode"));
        try {
            s.setSolutions(mapper.readValue(rs.getString("solutionsJson"),
                    new TypeReference<List<ExpectedSolution.SolutionEntry>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize solutions JSONB", e);
        }
        return s;
    }; }

    public ExpectedSolution findById(String id) {
        try {
            return jdbc.queryForObject(queries.FIND_BY_ID, rowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<ExpectedSolution> findByQuestionId(String questionId) {
        return jdbc.query(queries.FIND_BY_QUESTION_ID, rowMapper(), questionId);
    }

    public List<ExpectedSolution> findByDatasetId(String datasetId) {
        return jdbc.query(queries.FIND_BY_DATASET_ID, rowMapper(), datasetId);
    }

    public List<ExpectedSolution> findByQuestionIdAndSqlMode(String questionId, String sqlMode) {
        return jdbc.query(queries.FIND_BY_QUESTION_AND_MODE, rowMapper(), questionId, sqlMode);
    }

    public ExpectedSolution save(ExpectedSolution s) {
        try {
            if (s.getId() == null) {
                String id = jdbc.queryForObject(queries.INSERT, String.class,
                        s.getQuestionId(), s.getDatasetId(), s.getSqlMode(),
                        mapper.writeValueAsString(s.getSolutions()),
                        s.getCreatedAt());
                s.setId(id);
            } else {
                jdbc.update(queries.UPDATE,
                        s.getQuestionId(), s.getDatasetId(), s.getSqlMode(),
                        mapper.writeValueAsString(s.getSolutions()),
                        s.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save expected solution", e);
        }
        return s;
    }

    public void deleteById(String id) {
        jdbc.update(queries.DELETE_BY_ID, id);
    }
}
