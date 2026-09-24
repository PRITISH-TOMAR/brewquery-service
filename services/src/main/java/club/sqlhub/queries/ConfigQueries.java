package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class ConfigQueries {

    // Active permissions for a user — expired subscription rows are excluded
    public final String FIND_ACTIVE_PERMISSIONS = """
            SELECT module_key AS moduleKey,
                   operation  AS operation,
                   source     AS source,
                   expires_at AS expiresAt
            FROM   user_module_permissions
            WHERE  user_id   = ?
              AND  is_granted = true
              AND  (expires_at IS NULL OR expires_at > NOW())
            """;

    // Active subscriptions for a user, one row per module (latest active plan)
    public final String FIND_ACTIVE_SUBSCRIPTIONS = """
            SELECT DISTINCT ON (module_key)
                   module_key AS moduleKey,
                   plan_id    AS planId,
                   end_at     AS endAt
            FROM   subscriptions
            WHERE  user_id = ?
              AND  status  = 'ACTIVE'
              AND  end_at  > NOW()
            ORDER  BY module_key, end_at DESC
            """;

    // Seed default permissions on registration — skips if a row already exists
    public final String SEED_PERMISSION = """
            INSERT INTO user_module_permissions (user_id, module_key, operation, is_granted, source)
            VALUES (?, ?, ?, true, 'ROLE_DEFAULT')
            ON CONFLICT (user_id, module_key, operation) DO NOTHING
            """;

    // Admin scope rows for a given admin user
    public final String FIND_ADMIN_SCOPE = """
            SELECT module_key         AS moduleKey,
                   grantable_ops::TEXT AS grantableOps
            FROM   admin_module_scope
            WHERE  admin_user_id = ?
            """;
}
