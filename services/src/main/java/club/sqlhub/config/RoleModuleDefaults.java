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
        // Starts from USER defaults, then adds remaining operations on every module.
        // This guarantees ADMIN always has everything a USER has, plus more.
        Set<ModuleOperationEnum> allOps = EnumSet.allOf(ModuleOperationEnum.class);
        Map<ModuleEnum, Set<ModuleOperationEnum>> adminDefaults = new EnumMap<>(ModuleEnum.class);
        for (ModuleEnum module : ModuleEnum.values()) {
            Set<ModuleOperationEnum> ops = EnumSet.copyOf(
                userDefaults.getOrDefault(module, EnumSet.noneOf(ModuleOperationEnum.class))
            );
            ops.addAll(allOps);
            adminDefaults.put(module, ops);
        }
        map.put("ADMIN", Collections.unmodifiableMap(adminDefaults));

        // ── SUPERADMIN ────────────────────────────────────────────────────────
        // Starts from ADMIN defaults — inherits USER + ADMIN privileges.
        Map<ModuleEnum, Set<ModuleOperationEnum>> superAdminDefaults = new EnumMap<>(ModuleEnum.class);
        for (ModuleEnum module : ModuleEnum.values()) {
            Set<ModuleOperationEnum> ops = EnumSet.copyOf(adminDefaults.get(module));
            ops.addAll(allOps);
            superAdminDefaults.put(module, ops);
        }
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
