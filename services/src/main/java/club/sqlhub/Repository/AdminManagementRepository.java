package club.sqlhub.Repository;

import club.sqlhub.queries.AdminManagementQueries;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdminManagementRepository {

    private final JdbcTemplate          jdbc;
    private final AdminManagementQueries queries;

    // ── Listing ───────────────────────────────────────────────────────────────

    public List<Map<String, Object>> listUsersBelowAdmin() {
        return jdbc.queryForList(queries.LIST_USERS_BELOW_ADMIN);
    }

    public List<Map<String, Object>> listUsersBelowSuperAdmin() {
        return jdbc.queryForList(queries.LIST_USERS_BELOW_SUPERADMIN);
    }

    // ── Single user ───────────────────────────────────────────────────────────

    /** Returns null when not found (avoids exception bubbling). */
    public Map<String, Object> getUserById(Integer userId) {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(queries.GET_USER_BY_ID, userId);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    public void updateUserStatus(Integer userId, String status) {
        jdbc.update(queries.UPDATE_USER_STATUS, status, userId);
    }

    public void updateUserRole(Integer userId, String role) {
        jdbc.update(queries.UPDATE_USER_ROLE, role, userId);
    }

    // ── Permissions ───────────────────────────────────────────────────────────

    public List<Map<String, Object>> getUserPermissions(Integer userId) {
        return jdbc.queryForList(queries.GET_USER_PERMISSIONS, userId);
    }

    public void upsertPermission(Integer userId, String moduleKey, String operation) {
        jdbc.update(queries.UPSERT_PERMISSION, userId, moduleKey, operation);
    }

    public void revokePermission(Integer userId, String moduleKey, String operation) {
        jdbc.update(queries.REVOKE_PERMISSION, userId, moduleKey, operation);
    }

    // ── Admin scope ───────────────────────────────────────────────────────────

    public List<Map<String, Object>> listAdminsWithScope() {
        return jdbc.queryForList(queries.LIST_ADMINS_WITH_SCOPE);
    }

    public List<Map<String, Object>> findAdminScope(Integer adminUserId) {
        return jdbc.queryForList(queries.FIND_ADMIN_SCOPE, adminUserId);
    }

    public void upsertAdminScope(Integer adminUserId, String moduleKey, String grantableOpsJson) {
        jdbc.update(queries.UPSERT_ADMIN_SCOPE, adminUserId, moduleKey, grantableOpsJson);
    }
}
