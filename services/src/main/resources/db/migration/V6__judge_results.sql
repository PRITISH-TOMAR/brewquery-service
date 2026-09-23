CREATE TABLE judge_results (
    id             BIGSERIAL    PRIMARY KEY,
    job_id         VARCHAR(100) NOT NULL UNIQUE,
    user_id        VARCHAR(50),
    question_id    VARCHAR(50),
    question_title TEXT,
    dataset        VARCHAR(500),
    level          VARCHAR(50),
    language       VARCHAR(50),
    submitted_at   TIMESTAMP,
    result         JSONB
);

CREATE INDEX idx_jr_job_id      ON judge_results(job_id);
CREATE INDEX idx_jr_user_id     ON judge_results(user_id);
CREATE INDEX idx_jr_question_id ON judge_results(question_id);
CREATE INDEX idx_jr_submitted   ON judge_results(submitted_at DESC);
