package club.sqlhub.config;

import club.sqlhub.entity.Enums.ModuleEnum;
import club.sqlhub.entity.Enums.ModuleOperationEnum;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static club.sqlhub.entity.Enums.ModuleOperationEnum.*;

/**
 * Hardcoded default permissions seeded into user_module_permissions
 * when a new user is registered. Source = ROLE_DEFAULT, expires_at = null.
 *
 * Extend this map when new modules or roles are introduced.
 */
public final class RoleModuleDefaults {

    private RoleModuleDefaults() {}

    private static final Map<String, Map<ModuleEnum, Set<ModuleOperationEnum>>> DEFAULTS;

    static {
        Map<String, Map<ModuleEnum, Set<ModuleOperationEnum>>> map = new java.util.HashMap<>();

        // ── USER ─────────────────────────────────────────────────────────────
        // READ on all modules by default. Additional ops require admin grant or subscription.
        Map<ModuleEnum, Set<ModuleOperationEnum>> userDefaults = new EnumMap<>(ModuleEnum.class);
        userDefaults.put(ModuleEnum.SQL,      EnumSet.of(READ));
        userDefaults.put(ModuleEnum.NOSQL,    EnumSet.of(READ));
        userDefaults.put(ModuleEnum.VECTORDB, EnumSet.of(READ));
        map.put("USER", Collections.unmodifiableMap(userDefaults));

        // ── ADMIN ─────────────────────────────────────────────────────────────
        // Full access to all modules by default.
        // Admin scope (which users they can manage) is controlled separately via admin_module_scope.
        Map<ModuleEnum, Set<ModuleOperationEnum>> adminDefaults = new EnumMap<>(ModuleEnum.class);
        Set<ModuleOperationEnum> allOps = EnumSet.allOf(ModuleOperationEnum.class);
        adminDefaults.put(ModuleEnum.SQL,      EnumSet.copyOf(allOps));
        adminDefaults.put(ModuleEnum.NOSQL,    EnumSet.copyOf(allOps));
        adminDefaults.put(ModuleEnum.VECTORDB, EnumSet.copyOf(allOps));
        map.put("ADMIN", Collections.unmodifiableMap(adminDefaults));

        // ── SUPERADMIN ────────────────────────────────────────────────────────
        // Same as admin — full access. Superadmin privilege is enforced at the
        // service layer, not through extra operations.
        Map<ModuleEnum, Set<ModuleOperationEnum>> superAdminDefaults = new EnumMap<>(ModuleEnum.class);
        superAdminDefaults.put(ModuleEnum.SQL,      EnumSet.copyOf(allOps));
        superAdminDefaults.put(ModuleEnum.NOSQL,    EnumSet.copyOf(allOps));
        superAdminDefaults.put(ModuleEnum.VECTORDB, EnumSet.copyOf(allOps));
        map.put("SUPERADMIN", Collections.unmodifiableMap(superAdminDefaults));

        DEFAULTS = Collections.unmodifiableMap(map);
    }

    /**
     * Returns the default module → operations map for a given role.
     * Returns an empty map if the role is unrecognised.
     */
    public static Map<ModuleEnum, Set<ModuleOperationEnum>> getDefaults(String role) {
        return DEFAULTS.getOrDefault(role.toUpperCase(), Collections.emptyMap());
    }
}
