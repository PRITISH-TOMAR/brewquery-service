package club.sqlhub.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import club.sqlhub.entity.admin.request.*;
import club.sqlhub.entity.admin.response.*;
import club.sqlhub.entity.user.DTO.profile.*;
import club.sqlhub.service.AdminManagementService;
import club.sqlhub.service.UserService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.access.RequiresAccess;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping
public class UserController {

    private final UserService            userService;
    private final AdminManagementService adminManagementService;

    /** Full profile card: name, contact, level, header stats. */
    @GetMapping("/user/{userId}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getProfile(
            @PathVariable Integer userId) {
        return userService.getProfile(userId);
    }

    /**
     * Submission aggregates: totals, difficulty breakdown,
     * favourite datasets, recent badges.
     * Query param {@code period}: all | year | month (default: all)
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<ApiResponse<UserStatsResponseDTO>> getStats(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "all") String period) {
        return userService.getStats(userId, period);
    }

    /**
     * Recent submissions list.
     * Query param {@code limit}: number of rows to return (default: 5)
     */
    @GetMapping("/user/{userId}/submissions")
    public ResponseEntity<ApiResponse<List<UserSubmissionDTO>>> getSubmissions(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "5") int limit) {
        return userService.getSubmissions(userId, limit);
    }

    /** Daily submission counts for the activity heatmap. */
    @GetMapping("/user/{userId}/heatmap")
    public ResponseEntity<ApiResponse<List<HeatmapEntryDTO>>> getHeatmap(
            @PathVariable Integer userId) {
        return userService.getHeatmap(userId);
    }

    /** Upload / replace the user's avatar. Accepts multipart/form-data with field "file". */
    @PutMapping(value = "/user/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @PathVariable Integer userId,
            @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(userId, file);
    }

    // ── User management (ADMIN, SUPERADMIN) ───────────────────────────────────

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserSummaryDTO>>> listUsers() {
        return adminManagementService.listUsers();
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponseDTO>> getUserDetail(
            @PathVariable Integer id) {
        AdminUserDetailRequestDTO req = new AdminUserDetailRequestDTO();
        req.setUserId(id);
        return adminManagementService.getUserDetail(req);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody AdminUserStatusRequestDTO req) {
        req.setUserId(id);
        return adminManagementService.updateStatus(req);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<Void>> updateRole(
            @PathVariable Integer id,
            @Valid @RequestBody AdminUserRoleRequestDTO req) {
        req.setUserId(id);
        return adminManagementService.updateRole(req);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PutMapping("/users/{id}/permissions")
    public ResponseEntity<ApiResponse<Void>> updatePermissions(
            @PathVariable Integer id,
            @Valid @RequestBody AdminPermissionsRequestDTO req) {
        req.setUserId(id);
        return adminManagementService.updatePermissions(req);
    }

    // ── Admin scope (SUPERADMIN only) ─────────────────────────────────────────

    @RequiresAccess(roles = {"SUPERADMIN"})
    @GetMapping("/users/admin-scopes")
    public ResponseEntity<ApiResponse<List<AdminScopeSummaryDTO>>> listAdminScopes() {
        return adminManagementService.listAdminsWithScopes();
    }

    @RequiresAccess(roles = {"SUPERADMIN"})
    @PutMapping("/users/admin-scopes/{adminId}")
    public ResponseEntity<ApiResponse<Void>> setAdminScope(
            @PathVariable Integer adminId,
            @Valid @RequestBody SuperAdminScopeRequestDTO req) {
        req.setAdminId(adminId);
        return adminManagementService.setAdminScope(req);
    }
}
