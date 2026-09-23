package club.sqlhub.Repository;

import java.sql.Array;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.mongo.models.TestCaseSQL.TestCase;
import club.sqlhub.mongo.models.TestCaseSQL.TestCases;
import club.sqlhub.queries.TestCaseQueries;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TestCaseSQLRepository {

    private final JdbcTemplate    jdbc;
    private final TestCaseQueries queries;
    private final ObjectMapper    mapper;

    private RowMapper<TestCases> rowMapper() { return (rs, rn) -> {
        TestCases tc = new TestCases();
        tc.setId(rs.getString("id"));
        tc.setQuestionId(rs.getString("questionId"));
        tc.setType(rs.getString("type"));
        tc.setExpectedSql(rs.getString("expectedSql"));
        try {
            tc.setTestCases(mapper.readValue(rs.getString("testCasesJson"),
                    new TypeReference<List<TestCase>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize test_cases JSONB", e);
        }
        return tc;
    }; }

    public Optional<TestCases> findById(String id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(queries.FIND_BY_ID, rowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<TestCases> findByQuestionId(String questionId) {
        List<TestCases> results = jdbc.query(queries.FIND_BY_QUESTION_ID, rowMapper(), questionId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<TestCases> findAll() {
        return jdbc.query(queries.FIND_ALL, rowMapper());
    }

    public List<TestCases> findByQuestionIds(List<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) return Collections.emptyList();
        try {
            Array arr = jdbc.getDataSource().getConnection()
                    .createArrayOf("VARCHAR", questionIds.toArray(new String[0]));
            return jdbc.query(queries.FIND_BY_QUESTION_IDS, rowMapper(), arr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query test cases by question ids", e);
        }
    }

    public String findExpectedSqlByQuestionId(String questionId) {
        try {
            return jdbc.queryForObject(queries.FIND_EXPECTED_SQL, String.class, questionId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public TestCases save(TestCases tc) {
        try {
            if (tc.getId() == null) {
                String id = jdbc.queryForObject(queries.INSERT, String.class,
                        tc.getQuestionId(), tc.getType(), tc.getExpectedSql(),
                        mapper.writeValueAsString(tc.getTestCases()));
                tc.setId(id);
            } else {
                jdbc.update(queries.UPDATE,
                        tc.getType(), tc.getExpectedSql(),
                        mapper.writeValueAsString(tc.getTestCases()),
                        tc.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save test case group", e);
        }
        return tc;
    }

    public void deleteById(String id) {
        jdbc.update(queries.DELETE_BY_ID, id);
    }
}
