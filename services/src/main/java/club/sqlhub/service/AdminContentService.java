package club.sqlhub.service;

import java.util.List;
import java.util.Set;

import club.sqlhub.Repository.DatasetSQLRepository;
import club.sqlhub.Repository.ExpectedSolutionSQLRepository;
import club.sqlhub.Repository.MetadataSQLRepository;
import club.sqlhub.Repository.ProblemSQLRepository;
import club.sqlhub.Repository.TestCaseSQLRepository;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.Datasets.DatasetPageResponseDTO;
import club.sqlhub.entity.Datasets.ProblemDescription;
import club.sqlhub.entity.admin.request.AdminDatasetRequestDTO;
import club.sqlhub.entity.admin.request.AdminExpectedSolutionRequestDTO;
import club.sqlhub.entity.admin.request.AdminProblemRequestDTO;
import club.sqlhub.entity.admin.request.AdminTestCaseGroupRequestDTO;
import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.mongo.models.ExpectedSolution;
import club.sqlhub.mongo.models.Metadata;
import club.sqlhub.mongo.models.Problem;
import club.sqlhub.mongo.models.Question;
import club.sqlhub.mongo.models.TestCaseSQL.TestCase;
import club.sqlhub.mongo.service.TestCaseService;
import club.sqlhub.mongo.models.TestCaseSQL.TestCases;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.Auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminContentService {

    private final DatasetSQLRepository          datasetRepo;
    private final ProblemSQLRepository          problemRepo;
    private final MetadataSQLRepository         metadataRepo;
    private final TestCaseSQLRepository         testCaseRepo;
    private final TestCaseService               testCaseService;
    private final ExpectedSolutionSQLRepository solutionRepo;

    // ── Auth helpers ──────────────────────────────────────────────────────────

    private UserPrincipal getPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private boolean isSuperAdmin() {
        return getPrincipal().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
    }

    private boolean canWrite(String moduleKey) {
        if (isSuperAdmin()) return true;
        return getPrincipal().getModulePermissions()
                .getOrDefault(moduleKey.toUpperCase(), java.util.Set.of())
                .contains("WRITE");
    }

    private boolean canDelete(String moduleKey) {
        if (isSuperAdmin()) return true;
        return getPrincipal().getModulePermissions()
                .getOrDefault(moduleKey.toUpperCase(), java.util.Set.of())
                .contains("DELETE");
    }

    // ── Dataset reads ─────────────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<DatasetPageResponseDTO>> getDatasets(
            String module, int page, int size, String search) {
        try {
            int offset = page * size;
            List<Dataset> items;
            long total;
            if (search != null && !search.isBlank()) {
                items = datasetRepo.findByModuleAndTitlePaged(module, search, size, offset);
                total = datasetRepo.countByModuleAndTitle(module, search);
            } else {
                items = datasetRepo.findByModulePaged(module, size, offset);
                total = datasetRepo.countByModule(module);
            }
            int totalPages = (int) Math.ceil((double) total / size);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK,
                    new DatasetPageResponseDTO(items, page, size, total, totalPages));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<Dataset>> getDatasetById(String id) {
        try {
            Dataset d = datasetRepo.findById(id);
            if (d == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, d);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── Dataset mutations ─────────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<Dataset>> createDataset(AdminDatasetRequestDTO req) {
        try {
            if (!canWrite(req.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            Dataset saved = datasetRepo.save(toDataset(req));
            return ApiResponse.call(HttpStatus.CREATED, MessageConstants.CONTENT_CREATED, saved);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<Dataset>> updateDataset(String id, AdminDatasetRequestDTO req) {
        try {
            Dataset existing = datasetRepo.findById(id);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canWrite(existing.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            Dataset d = toDataset(req);
            d.setId(id);
            d.setCreatedAt(existing.getCreatedAt());
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_UPDATED, datasetRepo.save(d));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<Void>> deleteDataset(String id) {
        try {
            Dataset existing = datasetRepo.findById(id);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canDelete(existing.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_DELETE_ACCESS);

            datasetRepo.softDeleteById(id);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_DELETED);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── Problem reads ─────────────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<List<Problem>>> getProblemsByDataset(String datasetId) {
        try {
            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK,
                    problemRepo.findByDatasetId(datasetId));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<ProblemDescription>> getProblemById(String id) {
        try {
            Problem p = problemRepo.findById(id);
            if (p == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.QUESTION_NOT_FOUND);

            Metadata metadata = metadataRepo.findByDatasetId(p.getDatasetId());
            Set<String> relevant = (p.getTableNames() != null && !p.getTableNames().isEmpty())
                    ? Set.copyOf(p.getTableNames()) : null;

            if (metadata != null && relevant != null) {
                metadata.setTables(metadata.getTables().stream()
                        .filter(t -> relevant.contains(t.getName()))
                        .collect(java.util.stream.Collectors.toList()));
            }

            List<TestCase> testCases = testCaseService.findTestCasesByQuestionId(id);
            if (relevant != null) {
                for (TestCase tc : testCases) {
                    if (tc.getSampleData() instanceof List) {
                        List<?> raw = (List<?>) tc.getSampleData();
                        tc.setSampleData(raw.stream()
                                .filter(e -> e instanceof java.util.Map && relevant.contains(((java.util.Map<?, ?>) e).get("table")))
                                .collect(java.util.stream.Collectors.toList()));
                    }
                }
            }

            // Map Problem → Question for ProblemDescription (identical fields)
            Question q = new Question();
            q.setId(p.getId());
            q.setDatasetId(p.getDatasetId());
            q.setTitle(p.getTitle());
            q.setQuestion(p.getQuestion());
            q.setDifficulty(p.getDifficulty());
            q.setTags(p.getTags());
            q.setType(p.getType());
            q.setTableNames(p.getTableNames());
            q.setCreatedAt(p.getCreatedAt());
            q.setDeletedAt(p.getDeletedAt());

            ProblemDescription res = new ProblemDescription();
            res.setQuestion(q);
            res.setMetadata(metadata);
            res.setTestCases(testCases);

            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, res);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── Problem mutations ─────────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<Problem>> createProblem(String datasetId, AdminProblemRequestDTO req) {
        try {
            Dataset dataset = datasetRepo.findById(datasetId);
            if (dataset == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canWrite(dataset.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            req.setDatasetId(datasetId);
            Problem saved = problemRepo.save(toProblem(req));
            datasetRepo.incrementQuestions(datasetId);
            return ApiResponse.call(HttpStatus.CREATED, MessageConstants.CONTENT_CREATED, saved);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<Problem>> updateProblem(String id, AdminProblemRequestDTO req) {
        try {
            Problem existing = problemRepo.findById(id);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.QUESTION_NOT_FOUND);

            Dataset dataset = datasetRepo.findById(existing.getDatasetId());
            if (dataset == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canWrite(dataset.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            Problem p = toProblem(req);
            p.setId(id);
            p.setCreatedAt(existing.getCreatedAt());
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_UPDATED, problemRepo.save(p));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<Void>> deleteProblem(String id) {
        try {
            Problem existing = problemRepo.findById(id);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.QUESTION_NOT_FOUND);

            Dataset dataset = datasetRepo.findById(existing.getDatasetId());
            if (dataset == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canDelete(dataset.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_DELETE_ACCESS);

            problemRepo.softDeleteById(id);
            datasetRepo.decrementQuestions(existing.getDatasetId());
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_DELETED);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── Test case reads ───────────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<TestCases>> getTestCaseByProblem(String problemId) {
        try {
            TestCases tc = testCaseRepo.findByQuestionId(problemId).orElse(null);
            if (tc == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.TESTCASE_NOT_FOUND);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, tc);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── Test case mutations ───────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<TestCases>> createTestCase(AdminTestCaseGroupRequestDTO req) {
        try {
            String moduleKey = resolveModuleByProblem(req.getQuestionId());
            if (moduleKey == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.QUESTION_NOT_FOUND);

            if (!canWrite(moduleKey))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            return ApiResponse.call(HttpStatus.CREATED, MessageConstants.CONTENT_CREATED,
                    testCaseRepo.save(toTestCases(req)));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<TestCases>> updateTestCase(String id, AdminTestCaseGroupRequestDTO req) {
        try {
            TestCases existing = testCaseRepo.findById(id).orElse(null);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.TESTCASE_NOT_FOUND);

            String moduleKey = resolveModuleByProblem(existing.getQuestionId());
            if (moduleKey == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.QUESTION_NOT_FOUND);

            if (!canWrite(moduleKey))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            TestCases tc = toTestCases(req);
            tc.setId(id);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_UPDATED, testCaseRepo.save(tc));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<Void>> deleteTestCase(String id) {
        try {
            TestCases existing = testCaseRepo.findById(id).orElse(null);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.TESTCASE_NOT_FOUND);

            String moduleKey = resolveModuleByProblem(existing.getQuestionId());
            if (moduleKey == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.QUESTION_NOT_FOUND);

            if (!canDelete(moduleKey))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_DELETE_ACCESS);

            testCaseRepo.deleteById(id);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_DELETED);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── Solution reads ────────────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<List<ExpectedSolution>>> getSolutionsByProblem(String problemId) {
        try {
            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK,
                    solutionRepo.findByQuestionId(problemId));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── Solution mutations ────────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<ExpectedSolution>> createSolution(AdminExpectedSolutionRequestDTO req) {
        try {
            Dataset dataset = datasetRepo.findById(req.getDatasetId());
            if (dataset == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canWrite(dataset.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            return ApiResponse.call(HttpStatus.CREATED, MessageConstants.CONTENT_CREATED,
                    solutionRepo.save(toSolution(req)));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<ExpectedSolution>> updateSolution(String id, AdminExpectedSolutionRequestDTO req) {
        try {
            ExpectedSolution existing = solutionRepo.findById(id);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.SOLUTION_NOT_FOUND);

            Dataset dataset = datasetRepo.findById(existing.getDatasetId());
            if (dataset == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canWrite(dataset.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            ExpectedSolution sol = toSolution(req);
            sol.setId(id);
            sol.setCreatedAt(existing.getCreatedAt());
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_UPDATED, solutionRepo.save(sol));
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<Void>> deleteSolution(String id) {
        try {
            ExpectedSolution existing = solutionRepo.findById(id);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.SOLUTION_NOT_FOUND);

            Dataset dataset = datasetRepo.findById(existing.getDatasetId());
            if (dataset == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canDelete(dataset.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_DELETE_ACCESS);

            solutionRepo.deleteById(id);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_DELETED);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── Resolvers ─────────────────────────────────────────────────────────────

    private String resolveModuleByProblem(String problemId) {
        Problem p = problemRepo.findById(problemId);
        if (p == null) return null;
        Dataset d = datasetRepo.findById(p.getDatasetId());
        if (d == null) return null;
        return d.getDataType();
    }

    // ── DTO → Model converters ────────────────────────────────────────────────

    private Dataset toDataset(AdminDatasetRequestDTO req) {
        Dataset d = new Dataset();
        d.setSlug(req.getSlug());
        d.setTitle(req.getTitle());
        d.setDescription(req.getDescription());
        d.setIcon(req.getIcon());
        d.setDifficulty(req.getDifficulty());
        d.setDataType(req.getDataType());
        d.setEstimatedTime(req.getEstimatedTime());
        d.setTags(req.getTags());
        d.setCategories(req.getCategories());
        d.setSkills(req.getSkills());
        d.setModesAvailable(req.getModesAvailable());
        d.setTableCount(req.getTableCount());
        return d;
    }

    private Problem toProblem(AdminProblemRequestDTO req) {
        Problem p = new Problem();
        p.setDatasetId(req.getDatasetId());
        p.setTitle(req.getTitle());
        p.setQuestion(req.getQuestion());
        p.setDifficulty(req.getDifficulty());
        p.setType(req.getType());
        p.setTags(req.getTags());
        p.setTableNames(req.getTableNames());
        return p;
    }

    private TestCases toTestCases(AdminTestCaseGroupRequestDTO req) {
        TestCases tc = new TestCases();
        tc.setQuestionId(req.getQuestionId());
        tc.setType(req.getType());
        tc.setExpectedSql(req.getExpectedSql());
        tc.setTestCases(req.getTestCases());
        return tc;
    }

    private ExpectedSolution toSolution(AdminExpectedSolutionRequestDTO req) {
        ExpectedSolution sol = new ExpectedSolution();
        sol.setQuestionId(req.getQuestionId());
        sol.setDatasetId(req.getDatasetId());
        sol.setSqlMode(req.getSqlMode());
        sol.setSolutions(req.getSolutions());
        return sol;
    }
}
