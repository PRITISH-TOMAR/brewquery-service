package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class JudgeResultQueries {

    private static final String SELECT = """
            SELECT job_id AS jobId, user_id AS userId, question_id AS questionId,
                   question_title AS questionTitle, dataset, level, language,
                   submitted_at AS submittedAt, result::TEXT AS resultJson
            FROM judge_results
            """;

    public final String FIND_BY_JOB_ID = SELECT + "WHERE job_id = ?";

    public final String FIND_BY_USER_ID = SELECT + "WHERE user_id = ? ORDER BY submitted_at DESC";

    public final String FIND_BY_FILTERS = """
            SELECT job_id AS jobId, user_id AS userId, question_id AS questionId,
                   question_title AS questionTitle, dataset, level, language,
                   submitted_at AS submittedAt, result::TEXT AS resultJson
            FROM judge_results
            WHERE user_id = ?
              AND (CAST(? AS VARCHAR) IS NULL OR job_id = ?)
              AND (CAST(? AS VARCHAR) IS NULL OR question_id = ?)
              AND (CAST(? AS TIMESTAMP) IS NULL OR submitted_at >= CAST(? AS TIMESTAMP))
              AND (CAST(? AS TIMESTAMP) IS NULL OR submitted_at <= CAST(? AS TIMESTAMP))
              AND (CAST(? AS VARCHAR) IS NULL OR result->>'overallStatus' = ?)
            ORDER BY submitted_at DESC
            LIMIT ? OFFSET ?
            """;

    public final String COUNT_BY_FILTERS = """
            SELECT COUNT(*) FROM judge_results
            WHERE user_id = ?
              AND (CAST(? AS VARCHAR) IS NULL OR job_id = ?)
              AND (CAST(? AS VARCHAR) IS NULL OR question_id = ?)
              AND (CAST(? AS TIMESTAMP) IS NULL OR submitted_at >= CAST(? AS TIMESTAMP))
              AND (CAST(? AS TIMESTAMP) IS NULL OR submitted_at <= CAST(? AS TIMESTAMP))
              AND (CAST(? AS VARCHAR) IS NULL OR result->>'overallStatus' = ?)
            """;

    public final String UPSERT = """
            INSERT INTO judge_results
                (job_id, user_id, question_id, question_title, dataset,
                 level, language, submitted_at, result)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::JSONB)
            ON CONFLICT (job_id) DO UPDATE SET
                user_id        = EXCLUDED.user_id,
                question_id    = EXCLUDED.question_id,
                question_title = EXCLUDED.question_title,
                dataset        = EXCLUDED.dataset,
                level          = EXCLUDED.level,
                language       = EXCLUDED.language,
                submitted_at   = EXCLUDED.submitted_at,
                result         = EXCLUDED.result
            """;
}
