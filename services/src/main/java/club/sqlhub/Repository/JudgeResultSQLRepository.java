package club.sqlhub.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.queries.JudgeResultQueries;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JudgeResultSQLRepository {

    private final JdbcTemplate       jdbc;
    private final JudgeResultQueries queries;
    private final ObjectMapper       mapper;

    private final RowMapper<JudgeResultDTO> rowMapper = (rs, rn) -> {
        JudgeResultDTO d = new JudgeResultDTO();
        d.setJobId(rs.getString("jobId"));
        d.setUserId(rs.getString("userId"));
        d.setQuestionId(rs.getString("questionId"));
        d.setQuestionTitle(rs.getString("questionTitle"));
        d.setDataset(rs.getString("dataset"));
        d.setLevel(rs.getString("level"));
        d.setLanguage(rs.getString("language"));
        Timestamp ts = rs.getTimestamp("submittedAt");
        if (ts != null) d.setSubmittedAt(new Date(ts.getTime()));
        try {
            String json = rs.getString("resultJson");
            if (json != null) d.setResult(mapper.readValue(json, Object.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize judge result JSONB", e);
        }
        return d;
    };

    public JudgeResultDTO findByJobId(String jobId) {
        try {
            return jdbc.queryForObject(queries.FIND_BY_JOB_ID, rowMapper, jobId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<JudgeResultDTO> findByUserId(String userId) {
        return jdbc.query(queries.FIND_BY_USER_ID, rowMapper, userId);
    }

    public List<JudgeResultDTO> findByFilters(
            String userId, String jobId, String questionId,
            Date fromDate, Date toDate, String verdict, int limit, int offset) {

        Timestamp from = fromDate != null ? new Timestamp(fromDate.getTime()) : null;
        Timestamp to   = toDate   != null ? new Timestamp(toDate.getTime())   : null;

        return jdbc.query(queries.FIND_BY_FILTERS, rowMapper,
                userId,
                jobId, jobId,
                questionId, questionId,
                from, from,
                to, to,
                verdict, verdict,
                limit, offset);
    }

    public long countByFilters(
            String userId, String jobId, String questionId,
            Date fromDate, Date toDate, String verdict) {

        Timestamp from = fromDate != null ? new Timestamp(fromDate.getTime()) : null;
        Timestamp to   = toDate   != null ? new Timestamp(toDate.getTime())   : null;

        Long count = jdbc.queryForObject(queries.COUNT_BY_FILTERS, Long.class,
                userId,
                jobId, jobId,
                questionId, questionId,
                from, from,
                to, to,
                verdict, verdict);
        return count != null ? count : 0L;
    }

    public JudgeResultDTO save(JudgeResultDTO d) {
        try {
            Timestamp ts = d.getSubmittedAt() != null ? new Timestamp(d.getSubmittedAt().getTime()) : null;
            jdbc.update(queries.UPSERT,
                    d.getJobId(), d.getUserId(), d.getQuestionId(), d.getQuestionTitle(),
                    d.getDataset(), d.getLevel(), d.getLanguage(), ts,
                    mapper.writeValueAsString(d.getResult()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save judge result", e);
        }
        return d;
    }
}
