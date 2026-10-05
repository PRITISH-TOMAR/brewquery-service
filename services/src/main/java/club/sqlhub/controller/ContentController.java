package club.sqlhub.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import club.sqlhub.entity.Datasets.DatasetPageResponseDTO;
import club.sqlhub.entity.Datasets.ProblemDescription;
import club.sqlhub.entity.admin.request.*;
import club.sqlhub.entity.admin.request.AdminSolutionGenerateRequestDTO;
import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.mongo.models.ExpectedSolution;
import club.sqlhub.mongo.models.Problem;
import club.sqlhub.mongo.models.TestCaseSQL.TestCases;
import club.sqlhub.service.AdminAssetService;
import club.sqlhub.service.AdminContentService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.access.RequiresAccess;

@RestController
@AllArgsConstructor
public class ContentController {

    private final AdminContentService adminContentService;
    private final AdminAssetService   adminAssetService;

    // ── Datasets ──────────────────────────────────────────────────────────────

    @RequiresAccess(roles = {"USER", "ADMIN", "SUPERADMIN"})
    @GetMapping("/module/{module}/datasets")
    public ResponseEntity<ApiResponse<DatasetPageResponseDTO>> getDatasets(
            @PathVariable String module,
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "10") int    size,
            @RequestParam(required = false)    String search) {
        return adminContentService.getDatasets(module, page, size, search);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PostMapping("/module/{module}/datasets")
    public ResponseEntity<ApiResponse<Dataset>> createDataset(
            @PathVariable String module,
            @Valid @RequestBody AdminDatasetRequestDTO req) {
        req.setDataType(module);
        return adminContentService.createDataset(req);
    }

    @RequiresAccess(roles = {"USER", "ADMIN", "SUPERADMIN"})
    @GetMapping("/module/{module}/datasets/{id}")
    public ResponseEntity<ApiResponse<Dataset>> getDatasetById(
            @PathVariable String module,
            @PathVariable String id) {
        return adminContentService.getDatasetById(id);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PutMapping("/module/{module}/datasets/{id}")
    public ResponseEntity<ApiResponse<Dataset>> updateDataset(
            @PathVariable String module,
            @PathVariable String id,
            @Valid @RequestBody AdminDatasetRequestDTO req) {
        req.setDataType(module);
        return adminContentService.updateDataset(id, req);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @DeleteMapping("/module/{module}/datasets/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDataset(
            @PathVariable String module,
            @PathVariable String id) {
        return adminContentService.deleteDataset(id);
    }

    // ── Problems ──────────────────────────────────────────────────────────────

    @RequiresAccess(roles = {"USER", "ADMIN", "SUPERADMIN"})
    @GetMapping("/datasets/{datasetId}/problems")
    public ResponseEntity<ApiResponse<List<Problem>>> getProblemsByDataset(
            @PathVariable String datasetId) {
        return adminContentService.getProblemsByDataset(datasetId);
    }

    @RequiresAccess(roles = {"USER", "ADMIN", "SUPERADMIN"})
    @GetMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<ProblemDescription>> getProblemById(@PathVariable String id) {
        return adminContentService.getProblemById(id);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PostMapping("/datasets/{datasetId}/problems")
    public ResponseEntity<ApiResponse<Problem>> createProblem(
            @PathVariable String datasetId,
            @Valid @RequestBody AdminProblemRequestDTO req) {
        return adminContentService.createProblem(datasetId, req);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PatchMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<Problem>> updateProblem(
            @PathVariable String id,
            @Valid @RequestBody AdminProblemRequestDTO req) {
        return adminContentService.updateProblem(id, req);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @DeleteMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(@PathVariable String id) {
        return adminContentService.deleteProblem(id);
    }

    // ── Test cases ────────────────────────────────────────────────────────────

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @GetMapping("/problems/{problemId}/testcases")
    public ResponseEntity<ApiResponse<TestCases>> getTestCaseByProblem(
            @PathVariable String problemId) {
        return adminContentService.getTestCaseByProblem(problemId);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PostMapping("/testcases")
    public ResponseEntity<ApiResponse<TestCases>> createTestCase(
            @Valid @RequestBody AdminTestCaseGroupRequestDTO req) {
        return adminContentService.createTestCase(req);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PutMapping("/testcases/{id}")
    public ResponseEntity<ApiResponse<TestCases>> updateTestCase(
            @PathVariable String id,
            @Valid @RequestBody AdminTestCaseGroupRequestDTO req) {
        return adminContentService.updateTestCase(id, req);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @DeleteMapping("/testcases/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTestCase(@PathVariable String id) {
        return adminContentService.deleteTestCase(id);
    }

    // ── Solutions ─────────────────────────────────────────────────────────────

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @GetMapping("/problems/{problemId}/solutions")
    public ResponseEntity<ApiResponse<List<ExpectedSolution>>> getSolutionsByProblem(
            @PathVariable String problemId) {
        return adminContentService.getSolutionsByProblem(problemId);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PostMapping("/solutions")
    public ResponseEntity<ApiResponse<ExpectedSolution>> createSolution(
            @Valid @RequestBody AdminExpectedSolutionRequestDTO req) {
        return adminContentService.createSolution(req);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PutMapping("/solutions/{id}")
    public ResponseEntity<ApiResponse<ExpectedSolution>> updateSolution(
            @PathVariable String id,
            @Valid @RequestBody AdminExpectedSolutionRequestDTO req) {
        return adminContentService.updateSolution(id, req);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @DeleteMapping("/solutions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSolution(@PathVariable String id) {
        return adminContentService.deleteSolution(id);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PostMapping("/problems/{questionId}/solution/generate")
    public ResponseEntity<ApiResponse<ExpectedSolution>> generateSolution(
            @PathVariable String questionId,
            @Valid @RequestBody AdminSolutionGenerateRequestDTO req) {
        return adminContentService.generateAndSaveSolution(questionId, req);
    }

    // ── Assets ────────────────────────────────────────────────────────────────

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PostMapping(value = "/datasets/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadDatasetCover(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        return adminAssetService.uploadDatasetCover(id, file);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PostMapping(value = "/datasets/{id}/er", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadDatasetEr(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        return adminAssetService.uploadDatasetEr(id, file);
    }

    @RequiresAccess(roles = {"ADMIN", "SUPERADMIN"})
    @PostMapping(value = "/pages/{pageKey}/hero", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadPageHero(
            @PathVariable String pageKey,
            @RequestParam("file") MultipartFile file) {
        return adminAssetService.uploadPageHero(pageKey, file);
    }
}
