package club.sqlhub.service;

import club.sqlhub.Repository.AdminManagementRepository;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.admin.request.*;
import club.sqlhub.entity.admin.response.*;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.Auth.UserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminManagementService {

    private final AdminManagementRepository adminRepo;
    private final ObjectMapper              objectMapper;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UserPrincipal getPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getRequesterRole() {
        return getPrincipal().getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");
    }

    private Integer getRequesterId() {
        return Integer.parseInt(getPrincipal().getUsername());
    }

    private int roleRank(String role) {
        return switch (role.toUpperCase()) {
            case "SUPERADMIN" -> 3;
            case "ADMIN"      -> 2;
            case "USER"       -> 1;
            default           -> 0;
        };
    }

    // ── POST /admin/users/list ────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<List<AdminUserSummaryDTO>>> listUsers() {
        try {
            String requesterRole = getRequesterRole();
            List<Map<String, Object>> rows = "SUPERADMIN".equals(requesterRole)
                    ? adminRepo.listUsersBelowSuperAdmin()
                    : adminRepo.listUsersBelowAdmin();

            List<AdminUserSummaryDTO> list = rows.stream().map(this::toSummary).toList();
            return ApiResponse.call(HttpStatus.OK, MessageConstants.USERS_FETCHED, list);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── POST /admin/users/detail ──────────────────────────────────────────────

    public ResponseEntity<ApiResponse<AdminUserDetailResponseDTO>> getUserDetail(AdminUserDetailRequestDTO req) {
        try {
            String requesterRole = getRequesterRole();
            Map<String, Object> userRow = adminRepo.getUserById(req.getUserId());
            if (userRow == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.USER_NOT_FOUND);

            String targetRole = (String) userRow.get("role");
            if (roleRank(requesterRole) <= roleRank(targetRole))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.ACCESS_DENIED);

            List<Map<String, Object>> permRows = adminRepo.getUserPermissions(req.getUserId());
            List<AdminPermissionItemDTO> perms = permRows.stream().map(this::toPermission).toList();

            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, toDetail(userRow, perms));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── POST /admin/users/status ──────────────────────────────────────────────

    public ResponseEntity<ApiResponse<Void>> updateStatus(AdminUserStatusRequestDTO req) {
        try {
            String requesterRole = getRequesterRole();
            Map<String, Object> userRow = adminRepo.getUserById(req.getUserId());
            if (userRow == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.USER_NOT_FOUND);

            String targetRole = (String) userRow.get("role");
            if (roleRank(requesterRole) <= roleRank(targetRole))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.ACCESS_DENIED);

            String status = req.getStatus().toUpperCase();
            if (!"ACTIVE".equals(status) && !"BLOCKED".equals(status))
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.INVALID_STATUS);

            adminRepo.updateUserStatus(req.getUserId(), status);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.USER_STATUS_UPDATED);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── POST /admin/users/role ────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<Void>> updateRole(AdminUserRoleRequestDTO req) {
        try {
            String requesterRole = getRequesterRole();
            Map<String, Object> userRow = adminRepo.getUserById(req.getUserId());
            if (userRow == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.USER_NOT_FOUND);

            String targetRole = (String) userRow.get("role");
            String newRole    = req.getNewRole().toUpperCase();

            if (roleRank(requesterRole) <= roleRank(targetRole))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.ACCESS_DENIED);
            if (roleRank(requesterRole) <= roleRank(newRole))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.CANNOT_ASSIGN_ROLE);

            adminRepo.updateUserRole(req.getUserId(), newRole);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.USER_ROLE_UPDATED);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── POST /admin/users/permissions ────────────────────────────────────────

    public ResponseEntity<ApiResponse<Void>> updatePermissions(AdminPermissionsRequestDTO req) {
        try {
            String requesterRole = getRequesterRole();
            Map<String, Object> userRow = adminRepo.getUserById(req.getUserId());
            if (userRow == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.USER_NOT_FOUND);

            String targetRole = (String) userRow.get("role");
            if (roleRank(requesterRole) <= roleRank(targetRole))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.ACCESS_DENIED);

            // ADMIN: validate grants are within their module scope
            if ("ADMIN".equals(requesterRole) && req.getGrants() != null && !req.getGrants().isEmpty()) {
                List<Map<String, Object>> scopeRows = adminRepo.findAdminScope(getRequesterId());
                Map<String, Set<String>> allowedScope = buildScopeMap(scopeRows);
                for (AdminPermissionsRequestDTO.ModulePermissionEntry entry : req.getGrants()) {
                    Set<String> allowed = allowedScope.getOrDefault(entry.getModuleKey(), Set.of());
                    if (entry.getOperations() != null) {
                        for (String op : entry.getOperations()) {
                            if (!allowed.contains(op))
                                return ApiResponse.call(HttpStatus.FORBIDDEN,
                                        MessageConstants.OPERATION_OUT_OF_SCOPE + ": " + op);
                        }
                    }
                }
            }

            if (req.getGrants() != null) {
                for (AdminPermissionsRequestDTO.ModulePermissionEntry entry : req.getGrants()) {
                    if (entry.getOperations() != null) {
                        for (String op : entry.getOperations()) {
                            adminRepo.upsertPermission(req.getUserId(), entry.getModuleKey(), op);
                        }
                    }
                }
            }

            if (req.getRevokes() != null) {
                for (AdminPermissionsRequestDTO.ModulePermissionEntry entry : req.getRevokes()) {
                    if (entry.getOperations() != null) {
                        for (String op : entry.getOperations()) {
                            adminRepo.revokePermission(req.getUserId(), entry.getModuleKey(), op);
                        }
                    }
                }
            }

            return ApiResponse.call(HttpStatus.OK, MessageConstants.PERMISSIONS_UPDATED);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── GET /superadmin/scopes ────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<List<AdminScopeSummaryDTO>>> listAdminsWithScopes() {
        try {
            List<Map<String, Object>> rows = adminRepo.listAdminsWithScope();

            // Group rows by userId — one row per module per admin
            Map<Integer, AdminScopeSummaryDTO> adminMap = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) {
                Integer uid = (Integer) row.get("userId");
                adminMap.computeIfAbsent(uid, id -> {
                    AdminScopeSummaryDTO dto = new AdminScopeSummaryDTO();
                    dto.setUserId(uid);
                    dto.setName(row.get("firstName") + " " + row.get("lastName"));
                    dto.setEmail((String) row.get("email"));
                    dto.setStatus((String) row.get("status"));
                    dto.setScopes(new ArrayList<>());
                    return dto;
                });
                String moduleKey = (String) row.get("moduleKey");
                if (moduleKey != null) {
                    List<String> ops = parseJsonArray((String) row.get("grantableOps"));
                    adminMap.get(uid).getScopes().add(new AdminScopeItemDTO(moduleKey, ops));
                }
            }

            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, new ArrayList<>(adminMap.values()));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── POST /superadmin/scopes ───────────────────────────────────────────────

    public ResponseEntity<ApiResponse<Void>> setAdminScope(SuperAdminScopeRequestDTO req) {
        try {
            Map<String, Object> userRow = adminRepo.getUserById(req.getAdminId());
            if (userRow == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.USER_NOT_FOUND);
            if (!"ADMIN".equals(userRow.get("role")))
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.TARGET_NOT_ADMIN);

            List<String> ops = req.getGrantableOps() != null ? req.getGrantableOps() : List.of();
            adminRepo.upsertAdminScope(req.getAdminId(), req.getModuleKey(),
                    objectMapper.writeValueAsString(ops));

            return ApiResponse.call(HttpStatus.OK, MessageConstants.ADMIN_SCOPE_UPDATED);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private AdminUserSummaryDTO toSummary(Map<String, Object> row) {
        return new AdminUserSummaryDTO(
                (Integer) row.get("userId"),
                row.get("firstName") + " " + row.get("lastName"),
                (String) row.get("email"),
                (String) row.get("role"),
                (String) row.get("status"),
                row.get("createdAt") != null ? row.get("createdAt").toString() : null
        );
    }

    private AdminUserDetailResponseDTO toDetail(Map<String, Object> row,
                                                 List<AdminPermissionItemDTO> perms) {
        AdminUserDetailResponseDTO dto = new AdminUserDetailResponseDTO();
        dto.setUserId((Integer) row.get("userId"));
        dto.setName(row.get("firstName") + " " + row.get("lastName"));
        dto.setEmail((String) row.get("email"));
        dto.setRole((String) row.get("role"));
        dto.setStatus((String) row.get("status"));
        dto.setCreatedAt(row.get("createdAt") != null ? row.get("createdAt").toString() : null);
        dto.setPermissions(perms);
        return dto;
    }

    private AdminPermissionItemDTO toPermission(Map<String, Object> row) {
        Object expiresAtObj = row.get("expiresAt");
        return new AdminPermissionItemDTO(
                (String) row.get("moduleKey"),
                (String) row.get("operation"),
                (String) row.get("source"),
                Boolean.TRUE.equals(row.get("isGranted")),
                expiresAtObj != null ? expiresAtObj.toString() : null
        );
    }

    private Map<String, Set<String>> buildScopeMap(List<Map<String, Object>> scopeRows) {
        Map<String, Set<String>> map = new HashMap<>();
        for (Map<String, Object> row : scopeRows) {
            String moduleKey = (String) row.get("moduleKey");
            List<String> ops = parseJsonArray((String) row.get("grantableOps"));
            map.put(moduleKey, new HashSet<>(ops));
        }
        return map;
    }

    private List<String> parseJsonArray(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
