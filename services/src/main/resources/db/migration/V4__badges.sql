CREATE TABLE badge_definitions
(
    id          SERIAL       PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    color       VARCHAR(20)  NOT NULL
);

CREATE TABLE user_badges
(
    id        BIGSERIAL  PRIMARY KEY,
    user_id   INT        NOT NULL REFERENCES user_details(user_id),
    badge_id  INT        NOT NULL REFERENCES badge_definitions(id),
    earned_at TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, badge_id)
);

INSERT INTO badge_definitions (name, description, color) VALUES
('First Query',  'Solved 1 question',    '#f59e0b'),
('Week Streak',  '7 days in a row',      '#ef4444'),
('SQL Master',   'Solved 100 questions', '#a855f7'),
('Speed Demon',  'Solved in under 60s',  '#3b82f6'),
('Dataset Pro',  'Completed 5 datasets', '#22c55e');
