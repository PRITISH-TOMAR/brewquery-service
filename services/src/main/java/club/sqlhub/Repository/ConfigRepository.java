package club.sqlhub.Repository;

import club.sqlhub.queries.ConfigQueries;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ConfigRepository {

    private final JdbcTemplate jdbc;
    private final ConfigQueries queries;

    public List<Map<String, Object>> findActivePermissions(Integer userId) {
        return jdbc.queryForList(queries.FIND_ACTIVE_PERMISSIONS, userId);
    }

    public List<Map<String, Object>> findActiveSubscriptions(Integer userId) {
        return jdbc.queryForList(queries.FIND_ACTIVE_SUBSCRIPTIONS, userId);
    }

    public List<Map<String, Object>> findAdminScope(Integer userId) {
        return jdbc.queryForList(queries.FIND_ADMIN_SCOPE, userId);
    }

    public void seedPermissions(List<Object[]> rows) {
        jdbc.batchUpdate(queries.SEED_PERMISSION, rows);
    }
}
