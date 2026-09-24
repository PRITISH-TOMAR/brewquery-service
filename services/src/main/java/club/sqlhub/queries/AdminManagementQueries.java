package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class AdminManagementQueries {

    // ── User listing ──────────────────────────────────────────────────────────

    /** ADMIN sees only USERs */
    public final String LIST_USERS_BELOW_ADMIN = """
            SELECT user_id    AS userId,
                   first_name AS firstName,
                   last_name  AS lastName,
                   email,
                   role,
                   status,
                   created_at AS createdAt
            FROM   user_details
            WHERE  role = 'USER'
            ORDER  BY created_at DESC
            """;

    /** SUPERADMIN sees ADMINs and USERs */
    public final String LIST_USERS_BELOW_SUPERADMIN = """
            SELECT user_id    AS userId,
                   first_name AS firstName,
                   last_name  AS lastName,
                   email,
                   role,
                   status,
                   created_at AS createdAt
            FROM   user_details
            WHERE  role IN ('ADMIN', 'USER')
            ORDER  BY created_at DESC
            """;

    // ── Single user ───────────────────────────────────────────────────────────

    public final String GET_USER_BY_ID = """
            SELECT user_id    AS userId,
                   first_name AS firstName,
                   last_name  AS lastName,
                   email,
                   role,
                   status,
                   created_at AS createdAt
            FROM   user_details
            WHERE  user_id = ?
            """;

    // ── Status / role mutation ────────────────────────────────────────────────

    public final String UPDATE_USER_STATUS = """
            UPDATE user_details SET status = ? WHERE user_id = ?
            """;

    public final String UPDATE_USER_ROLE = """
            UPDATE user_details SET role = ? WHERE user_id = ?
            """;

    // ── Permissions ───────────────────────────────────────────────────────────

    public final String GET_USER_PERMISSIONS = """
            SELECT module_key  AS moduleKey,
                   operation,
                   source,
                   is_granted  AS isGranted,
                   expires_at  AS expiresAt
            FROM   user_module_permissions
            WHERE  user_id = ?
            ORDER  BY module_key, operation
            """;

    /** Upsert a granted permission (source = ADMIN_GRANT, clears expiry) */
    public final String UPSERT_PERMISSION = """
            INSERT INTO user_module_permissions (user_id, module_key, operation, is_granted, source)
            VALUES (?, ?, ?, true, 'ADMIN_GRANT')
            ON CONFLICT (user_id, module_key, operation) DO UPDATE
                SET is_granted = true,
                    source     = 'ADMIN_GRANT',
                    expires_at = NULL
            """;

    /** Revoke a permission (flip is_granted to false) */
    public final String REVOKE_PERMISSION = """
            UPDATE user_module_permissions
            SET    is_granted = false
            WHERE  user_id = ? AND module_key = ? AND operation = ?
            """;

    // ── Admin scope (SUPERADMIN) ──────────────────────────────────────────────

    /** All ADMINs joined with their scope rows (one row per module per admin) */
    public final String LIST_ADMINS_WITH_SCOPE = """
            SELECT ud.user_id          AS userId,
                   ud.first_name       AS firstName,
                   ud.last_name        AS lastName,
                   ud.email,
                   ud.status,
                   ams.module_key         AS moduleKey,
                   ams.grantable_ops::TEXT AS grantableOps
            FROM   user_details ud
            LEFT   JOIN admin_module_scope ams ON ams.admin_user_id = ud.user_id
            WHERE  ud.role = 'ADMIN'
            ORDER  BY ud.user_id
            """;

    /** Upsert a module scope row for an admin */
    public final String UPSERT_ADMIN_SCOPE = """
            INSERT INTO admin_module_scope (admin_user_id, module_key, grantable_ops)
            VALUES (?, ?, ?::jsonb)
            ON CONFLICT (admin_user_id, module_key) DO UPDATE
                SET grantable_ops = EXCLUDED.grantable_ops
            """;

    /** Fetch admin scope rows (re-used from ConfigQueries but kept here for self-containment) */
    public final String FIND_ADMIN_SCOPE = """
            SELECT module_key         AS moduleKey,
                   grantable_ops::TEXT AS grantableOps
            FROM   admin_module_scope
            WHERE  admin_user_id = ?
            """;

    /** Check if a user has a specific granted permission for a module+operation */
    public final String HAS_PERMISSION = """
            SELECT COUNT(*) FROM user_module_permissions
            WHERE  user_id = ? AND module_key = ? AND operation = ? AND is_granted = true
            """;
}
