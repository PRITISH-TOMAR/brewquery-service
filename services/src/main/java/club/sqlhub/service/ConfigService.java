package club.sqlhub.service;

import club.sqlhub.Repository.ConfigRepository;
import club.sqlhub.entity.Enums.ModuleEnum;
import club.sqlhub.entity.config.AdminScopeDTO;
import club.sqlhub.entity.config.ConfigResponseDTO;
import club.sqlhub.entity.config.ModuleConfigDTO;
import club.sqlhub.entity.config.SubscriptionDTO;
import club.sqlhub.utils.Auth.UserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.constants.MessageConstants;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;
    private final ObjectMapper objectMapper;

    public ResponseEntity<ApiResponse<ConfigResponseDTO>> getConfig() {

        UserPrincipal principal = (UserPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Integer userId = Integer.parseInt(principal.getUsername());
        String role    = principal.getAuthorities().iterator().next()
                                  .getAuthority().replace("ROLE_", "");

        // ── 1. Fetch active permissions ───────────────────────────────────────
        List<Map<String, Object>> permRows = configRepository.findActivePermissions(userId);

        // Group: moduleKey → Set<operation>
        Map<String, Set<String>> moduleOpsMap = new LinkedHashMap<>();
        for (ModuleEnum m : ModuleEnum.values()) {
            moduleOpsMap.put(m.name(), new LinkedHashSet<>());
        }
        for (Map<String, Object> row : permRows) {
            String mod = (String) row.get("moduleKey");
            String op  = (String) row.get("operation");
            if (moduleOpsMap.containsKey(mod)) {
                moduleOpsMap.get(mod).add(op);
            }
        }

        // ── 2. Fetch active subscriptions ─────────────────────────────────────
        List<Map<String, Object>> subRows = configRepository.findActiveSubscriptions(userId);

        // moduleKey → SubscriptionDTO
        Map<String, SubscriptionDTO> subscriptionMap = new HashMap<>();
        for (Map<String, Object> row : subRows) {
            String moduleKey = (String) row.get("moduleKey");
            String planId    = (String) row.get("planId");
            Timestamp endAt  = (Timestamp) row.get("endAt");
            subscriptionMap.put(moduleKey,
                    new SubscriptionDTO(planId, endAt.toLocalDateTime()));
        }

        // ── 3. Build module list ───────────────────────────────────────────────
        List<ModuleConfigDTO> modules = new ArrayList<>();
        for (ModuleEnum m : ModuleEnum.values()) {
            Set<String> ops = moduleOpsMap.get(m.name());
            modules.add(new ModuleConfigDTO(
                    m.name(),
                    !ops.isEmpty(),
                    new ArrayList<>(ops),
                    subscriptionMap.get(m.name())
            ));
        }

        // ── 4. Admin scope (only for ADMIN / SUPERADMIN) ──────────────────────
        List<AdminScopeDTO> adminScope = null;
        if ("ADMIN".equals(role) || "SUPERADMIN".equals(role)) {
            List<Map<String, Object>> scopeRows = configRepository.findAdminScope(userId);
            adminScope = new ArrayList<>();
            for (Map<String, Object> row : scopeRows) {
                String moduleKey    = (String) row.get("moduleKey");
                String grantableRaw = (String) row.get("grantableOps");
                List<String> grantableOps = parseJsonArray(grantableRaw);
                adminScope.add(new AdminScopeDTO(moduleKey, grantableOps));
            }
        }

        ConfigResponseDTO response = new ConfigResponseDTO(userId, role, modules, adminScope);
        return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, response);
    }

    private List<String> parseJsonArray(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
