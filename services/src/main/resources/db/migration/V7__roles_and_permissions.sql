-- ── USER MODULE PERMISSIONS ───────────────────────────────────────────────────
-- Stores the resolved, per-user permission set.
-- source: ROLE_DEFAULT (seeded on registration) | ADMIN_GRANT | SUBSCRIPTION
-- expires_at: NULL = permanent, set = subscription-bound (auto-revoked when past)
CREATE TABLE user_module_permissions
(
    id          BIGSERIAL    PRIMARY KEY,
    user_id     INT          NOT NULL,
    module_key  VARCHAR(50)  NOT NULL  CHECK (module_key IN ('SQL', 'NOSQL', 'VECTORDB')),
    operation   VARCHAR(50)  NOT NULL  CHECK (operation   IN ('READ', 'WRITE', 'EXECUTE', 'EXPORT', 'DELETE')),
    is_granted  BOOLEAN      NOT NULL  DEFAULT TRUE,
    source      VARCHAR(20)  NOT NULL  DEFAULT 'ROLE_DEFAULT'
                                       CHECK (source IN ('ROLE_DEFAULT', 'ADMIN_GRANT', 'SUBSCRIPTION')),
    granted_by  INT,
    expires_at  TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_ump_user_module_op  UNIQUE (user_id, module_key, operation),
    CONSTRAINT fk_ump_user            FOREIGN KEY (user_id)    REFERENCES user_details(user_id),
    CONSTRAINT fk_ump_granted_by      FOREIGN KEY (granted_by) REFERENCES user_details(user_id)
);

CREATE INDEX idx_ump_user_id     ON user_module_permissions(user_id);
CREATE INDEX idx_ump_user_module ON user_module_permissions(user_id, module_key);


-- ── ADMIN MODULE SCOPE ────────────────────────────────────────────────────────
-- Defines which modules an admin can manage and which operations they may grant.
-- grantable_ops: JSON array of operation strings, e.g. ["READ","WRITE"]
-- Only SUPERADMIN can insert/update rows here.
CREATE TABLE admin_module_scope
(
    id              BIGSERIAL    PRIMARY KEY,
    admin_user_id   INT          NOT NULL,
    module_key      VARCHAR(50)  NOT NULL  CHECK (module_key IN ('SQL', 'NOSQL', 'VECTORDB')),
    grantable_ops   JSONB        NOT NULL  DEFAULT '[]',
    granted_by      INT          NOT NULL,
    created_at      TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_ams_admin_module  UNIQUE (admin_user_id, module_key),
    CONSTRAINT fk_ams_admin         FOREIGN KEY (admin_user_id) REFERENCES user_details(user_id),
    CONSTRAINT fk_ams_granted_by    FOREIGN KEY (granted_by)    REFERENCES user_details(user_id)
);

CREATE INDEX idx_ams_admin_user_id ON admin_module_scope(admin_user_id);


-- ── SUBSCRIPTIONS ─────────────────────────────────────────────────────────────
-- Time-bound module access. On expiry, user_module_permissions rows
-- with source = 'SUBSCRIPTION' and expires_at <= NOW() are treated as revoked.
-- operations: JSON array of operations this plan grants, e.g. ["READ","EXECUTE"]
CREATE TABLE subscriptions
(
    id          BIGSERIAL    PRIMARY KEY,
    user_id     INT          NOT NULL,
    module_key  VARCHAR(50)  NOT NULL  CHECK (module_key IN ('SQL', 'NOSQL', 'VECTORDB')),
    plan_id     VARCHAR(100) NOT NULL,
    operations  JSONB        NOT NULL  DEFAULT '[]',
    start_at    TIMESTAMP    NOT NULL,
    end_at      TIMESTAMP    NOT NULL,
    status      VARCHAR(20)  NOT NULL  DEFAULT 'ACTIVE'
                                       CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED')),
    created_at  TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sub_user  FOREIGN KEY (user_id) REFERENCES user_details(user_id)
);

CREATE INDEX idx_sub_user_id     ON subscriptions(user_id);
CREATE INDEX idx_sub_user_status ON subscriptions(user_id, status);
