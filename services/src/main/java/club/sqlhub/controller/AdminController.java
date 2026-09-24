package club.sqlhub.controller;

import club.sqlhub.entity.admin.request.*;
import club.sqlhub.entity.admin.response.*;
import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.mongo.models.ExpectedSolution;
import club.sqlhub.mongo.models.Question;
import club.sqlhub.mongo.models.TestCaseSQL.TestCases;
import club.sqlhub.service.AdminAssetService;
import club.sqlhub.service.AdminContentService;
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
    private final AdminContentService    adminContentService;

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

    // ── Content management ────────────────────────────────────────────────────

    @PostMapping("/content/dataset")
    public ResponseEntity<ApiResponse<Dataset>> createDataset(
            @Valid @RequestBody AdminDatasetRequestDTO req) {
        return adminContentService.createDataset(req);
    }

    @PutMapping("/content/dataset/{id}")
    public ResponseEntity<ApiResponse<Dataset>> updateDataset(
            @PathVariable String id,
            @Valid @RequestBody AdminDatasetRequestDTO req) {
        return adminContentService.updateDataset(id, req);
    }

    @DeleteMapping("/content/dataset/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDataset(@PathVariable String id) {
        return adminContentService.deleteDataset(id);
    }

    @PostMapping("/content/question")
    public ResponseEntity<ApiResponse<Question>> createQuestion(
            @Valid @RequestBody AdminQuestionRequestDTO req) {
        return adminContentService.createQuestion(req);
    }

    @PutMapping("/content/question/{id}")
    public ResponseEntity<ApiResponse<Question>> updateQuestion(
            @PathVariable String id,
            @Valid @RequestBody AdminQuestionRequestDTO req) {
        return adminContentService.updateQuestion(id, req);
    }

    @DeleteMapping("/content/question/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable String id) {
        return adminContentService.deleteQuestion(id);
    }

    @PostMapping("/content/testcase")
    public ResponseEntity<ApiResponse<TestCases>> createTestCase(
            @Valid @RequestBody AdminTestCaseGroupRequestDTO req) {
        return adminContentService.createTestCase(req);
    }

    @PutMapping("/content/testcase/{id}")
    public ResponseEntity<ApiResponse<TestCases>> updateTestCase(
            @PathVariable String id,
            @Valid @RequestBody AdminTestCaseGroupRequestDTO req) {
        return adminContentService.updateTestCase(id, req);
    }

    @DeleteMapping("/content/testcase/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTestCase(@PathVariable String id) {
        return adminContentService.deleteTestCase(id);
    }

    @PostMapping("/content/solution")
    public ResponseEntity<ApiResponse<ExpectedSolution>> createSolution(
            @Valid @RequestBody AdminExpectedSolutionRequestDTO req) {
        return adminContentService.createSolution(req);
    }

    @PutMapping("/content/solution/{id}")
    public ResponseEntity<ApiResponse<ExpectedSolution>> updateSolution(
            @PathVariable String id,
            @Valid @RequestBody AdminExpectedSolutionRequestDTO req) {
        return adminContentService.updateSolution(id, req);
    }

    @DeleteMapping("/content/solution/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSolution(@PathVariable String id) {
        return adminContentService.deleteSolution(id);
    }
}
