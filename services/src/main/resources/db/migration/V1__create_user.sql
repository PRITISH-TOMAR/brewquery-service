CREATE TABLE user_roles
(
    role_id   SERIAL PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO user_roles (role_id, role_name) VALUES
(1, 'ADMIN'),
(2, 'USER');

CREATE TABLE user_details
(
    user_id             SERIAL PRIMARY KEY,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL UNIQUE,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED', 'IN_PROGRESS')),
    role_id             INT          NOT NULL DEFAULT 1,
    phone_number        VARCHAR(10),
    country_code        VARCHAR(7),
    profile_picture_url TEXT,
    hashed_password     TEXT         NOT NULL,
    salt                TEXT         NOT NULL,
    last_login          TIMESTAMP,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at         TIMESTAMP,
    CONSTRAINT fk_user_details_role FOREIGN KEY (role_id) REFERENCES user_roles(role_id)
);

CREATE UNIQUE INDEX idx_user_email ON user_details(email);
