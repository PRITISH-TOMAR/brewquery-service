package club.sqlhub.controller;

import club.sqlhub.entity.admin.request.*;
import club.sqlhub.entity.admin.response.*;
import club.sqlhub.service.AdminAssetService;
import club.sqlhub.service.AdminManagementService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminAssetService      adminAssetService;
    private final AdminManagementService adminManagementService;

    // ── User management ───────────────────────────────────────────────────────

    @PostMapping("/users/list")
    public ResponseEntity<ApiResponse<List<AdminUserSummaryDTO>>> listUsers() {
        return adminManagementService.listUsers();
    }

    @PostMapping("/users/detail")
    public ResponseEntity<ApiResponse<AdminUserDetailResponseDTO>> getUserDetail(
            @Valid @RequestBody AdminUserDetailRequestDTO req) {
        return adminManagementService.getUserDetail(req);
    }

    @PostMapping("/users/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @Valid @RequestBody AdminUserStatusRequestDTO req) {
        return adminManagementService.updateStatus(req);
    }

    @PostMapping("/users/role")
    public ResponseEntity<ApiResponse<Void>> updateRole(
            @Valid @RequestBody AdminUserRoleRequestDTO req) {
        return adminManagementService.updateRole(req);
    }

    @PostMapping("/users/permissions")
    public ResponseEntity<ApiResponse<Void>> updatePermissions(
            @Valid @RequestBody AdminPermissionsRequestDTO req) {
        return adminManagementService.updatePermissions(req);
    }

    // ── Asset management ──────────────────────────────────────────────────────

    @PostMapping(value = "/dataset/{datasetId}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadDatasetCover(
            @PathVariable String datasetId,
            @RequestParam("file") MultipartFile file) {
        return adminAssetService.uploadDatasetCover(datasetId, file);
    }

    @PostMapping(value = "/dataset/{datasetId}/er", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadDatasetEr(
            @PathVariable String datasetId,
            @RequestParam("file") MultipartFile file) {
        return adminAssetService.uploadDatasetEr(datasetId, file);
    }

    @PostMapping(value = "/page/{pageKey}/hero", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadPageHero(
            @PathVariable String pageKey,
            @RequestParam("file") MultipartFile file) {
        return adminAssetService.uploadPageHero(pageKey, file);
    }
}
