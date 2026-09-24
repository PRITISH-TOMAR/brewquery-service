package club.sqlhub.service;

import club.sqlhub.Repository.ConfigRepository;
import club.sqlhub.config.RoleModuleDefaults;
import club.sqlhub.entity.Enums.ModuleEnum;
import club.sqlhub.entity.Enums.ModuleOperationEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermissionSeedService {

    private final ConfigRepository configRepository;

    /**
     * Inserts default user_module_permissions rows for a newly registered user.
     * Called inside the registration @Transactional block — rolls back with the user on failure.
     */
    public void seed(Integer userId, String role) {
        Map<ModuleEnum, Set<ModuleOperationEnum>> defaults = RoleModuleDefaults.getDefaults(role);

        List<Object[]> rows = new ArrayList<>();
        for (Map.Entry<ModuleEnum, Set<ModuleOperationEnum>> entry : defaults.entrySet()) {
            String moduleKey = entry.getKey().name();
            for (ModuleOperationEnum op : entry.getValue()) {
                rows.add(new Object[]{ userId, moduleKey, op.name() });
            }
        }

        if (!rows.isEmpty()) {
            configRepository.seedPermissions(rows);
        }
    }
}
