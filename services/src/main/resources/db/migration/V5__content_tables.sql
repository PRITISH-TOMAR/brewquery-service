-- ── DATASETS ─────────────────────────────────────────────────────────────────
CREATE TABLE datasets (
    id                  VARCHAR(50)  PRIMARY KEY,
    slug                VARCHAR(100) UNIQUE,
    title               VARCHAR(500) NOT NULL,
    description         TEXT,
    icon                TEXT,
    cover_image         TEXT,
    er_image            TEXT,
    tags                JSONB        NOT NULL DEFAULT '[]',
    categories          JSONB        NOT NULL DEFAULT '[]',
    skills              JSONB        NOT NULL DEFAULT '[]',
    difficulty          VARCHAR(50),
    sql_modes_available JSONB        NOT NULL DEFAULT '[]',
    questions           INT          NOT NULL DEFAULT 0,
    table_count         INT          NOT NULL DEFAULT 0,
    data_type           VARCHAR(100),
    estimated_time      VARCHAR(100),
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ── QUESTIONS ────────────────────────────────────────────────────────────────
CREATE TABLE questions (
    id          VARCHAR(50)  PRIMARY KEY,
    dataset_id  VARCHAR(50)  NOT NULL REFERENCES datasets(id),
    title       VARCHAR(500) NOT NULL,
    question    TEXT,
    difficulty  VARCHAR(50),
    tags        JSONB        NOT NULL DEFAULT '[]',
    type        VARCHAR(50),
    table_names JSONB        NOT NULL DEFAULT '[]',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_questions_dataset_id ON questions(dataset_id);

-- ── METADATA ─────────────────────────────────────────────────────────────────
CREATE TABLE metadata (
    id     VARCHAR(50) PRIMARY KEY,
    tables JSONB       NOT NULL DEFAULT '[]'
);

-- ── TEST_CASE_GROUPS ─────────────────────────────────────────────────────────
CREATE TABLE test_case_groups (
    id           VARCHAR(50) PRIMARY KEY,
    question_id  VARCHAR(50) NOT NULL REFERENCES questions(id),
    type         VARCHAR(50),
    expected_sql TEXT,
    test_cases   JSONB       NOT NULL DEFAULT '[]'
);

CREATE INDEX idx_tcg_question_id ON test_case_groups(question_id);

-- ── EXPECTED_SOLUTIONS ────────────────────────────────────────────────────────
CREATE TABLE expected_solutions (
    id          VARCHAR(50) PRIMARY KEY,
    question_id VARCHAR(50) NOT NULL REFERENCES questions(id),
    dataset_id  VARCHAR(50) NOT NULL REFERENCES datasets(id),
    sql_mode    VARCHAR(50),
    solutions   JSONB       NOT NULL DEFAULT '[]',
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_es_question_id ON expected_solutions(question_id);
CREATE INDEX idx_es_dataset_id  ON expected_solutions(dataset_id);

-- ── PAGE_ASSETS ───────────────────────────────────────────────────────────────
CREATE TABLE page_assets (
    id              VARCHAR(50)  PRIMARY KEY,
    page_key        VARCHAR(100) NOT NULL UNIQUE,
    hero_image_url  TEXT,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
