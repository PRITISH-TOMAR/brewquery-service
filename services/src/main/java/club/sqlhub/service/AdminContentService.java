package club.sqlhub.service;

import club.sqlhub.Repository.AdminManagementRepository;
import club.sqlhub.Repository.DatasetSQLRepository;
import club.sqlhub.Repository.ExpectedSolutionSQLRepository;
import club.sqlhub.Repository.QuestionSQLRepository;
import club.sqlhub.Repository.TestCaseSQLRepository;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.admin.request.AdminDatasetRequestDTO;
import club.sqlhub.entity.admin.request.AdminExpectedSolutionRequestDTO;
import club.sqlhub.entity.admin.request.AdminQuestionRequestDTO;
import club.sqlhub.entity.admin.request.AdminTestCaseGroupRequestDTO;
import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.mongo.models.ExpectedSolution;
import club.sqlhub.mongo.models.Question;
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
    private final QuestionSQLRepository         questionRepo;
    private final TestCaseSQLRepository         testCaseRepo;
    private final ExpectedSolutionSQLRepository solutionRepo;
    private final AdminManagementRepository     adminRepo;

    // ── Auth helpers ──────────────────────────────────────────────────────────

    private Integer getCurrentUserId() {
        UserPrincipal p = (UserPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return Integer.parseInt(p.getUsername());
    }

    private boolean isSuperAdmin() {
        UserPrincipal p = (UserPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return p.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
    }

    private boolean canWrite(String moduleKey) {
        return isSuperAdmin() || adminRepo.hasPermission(getCurrentUserId(), moduleKey, "WRITE");
    }

    private boolean canDelete(String moduleKey) {
        return isSuperAdmin() || adminRepo.hasPermission(getCurrentUserId(), moduleKey, "DELETE");
    }

    // ── Dataset ───────────────────────────────────────────────────────────────

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
            Dataset saved = datasetRepo.save(d);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_UPDATED, saved);
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

    // ── Question ──────────────────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<Question>> createQuestion(AdminQuestionRequestDTO req) {
        try {
            Dataset dataset = datasetRepo.findById(req.getDatasetId());
            if (dataset == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canWrite(dataset.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            Question saved = questionRepo.save(toQuestion(req));
            datasetRepo.incrementQuestions(req.getDatasetId());
            return ApiResponse.call(HttpStatus.CREATED, MessageConstants.CONTENT_CREATED, saved);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<Question>> updateQuestion(String id, AdminQuestionRequestDTO req) {
        try {
            Question existing = questionRepo.findById(id);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.QUESTION_NOT_FOUND);

            Dataset dataset = datasetRepo.findById(existing.getDatasetId());
            if (dataset == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canWrite(dataset.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            Question q = toQuestion(req);
            q.setId(id);
            q.setCreatedAt(existing.getCreatedAt());
            Question saved = questionRepo.save(q);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_UPDATED, saved);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<Void>> deleteQuestion(String id) {
        try {
            Question existing = questionRepo.findById(id);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.QUESTION_NOT_FOUND);

            Dataset dataset = datasetRepo.findById(existing.getDatasetId());
            if (dataset == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canDelete(dataset.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_DELETE_ACCESS);

            questionRepo.softDeleteById(id);
            datasetRepo.decrementQuestions(existing.getDatasetId());
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_DELETED);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── TestCase Group ────────────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<TestCases>> createTestCase(AdminTestCaseGroupRequestDTO req) {
        try {
            String moduleKey = resolveModuleByQuestion(req.getQuestionId());
            if (moduleKey == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.QUESTION_NOT_FOUND);

            if (!canWrite(moduleKey))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            TestCases saved = testCaseRepo.save(toTestCases(req));
            return ApiResponse.call(HttpStatus.CREATED, MessageConstants.CONTENT_CREATED, saved);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<TestCases>> updateTestCase(String id, AdminTestCaseGroupRequestDTO req) {
        try {
            TestCases existing = testCaseRepo.findById(id).orElse(null);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.TESTCASE_NOT_FOUND);

            String moduleKey = resolveModuleByQuestion(existing.getQuestionId());
            if (moduleKey == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.QUESTION_NOT_FOUND);

            if (!canWrite(moduleKey))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            TestCases tc = toTestCases(req);
            tc.setId(id);
            TestCases saved = testCaseRepo.save(tc);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_UPDATED, saved);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<Void>> deleteTestCase(String id) {
        try {
            TestCases existing = testCaseRepo.findById(id).orElse(null);
            if (existing == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.TESTCASE_NOT_FOUND);

            String moduleKey = resolveModuleByQuestion(existing.getQuestionId());
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

    // ── Expected Solution ─────────────────────────────────────────────────────

    public ResponseEntity<ApiResponse<ExpectedSolution>> createSolution(AdminExpectedSolutionRequestDTO req) {
        try {
            Dataset dataset = datasetRepo.findById(req.getDatasetId());
            if (dataset == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            if (!canWrite(dataset.getDataType()))
                return ApiResponse.call(HttpStatus.FORBIDDEN, MessageConstants.NO_WRITE_ACCESS);

            ExpectedSolution saved = solutionRepo.save(toSolution(req));
            return ApiResponse.call(HttpStatus.CREATED, MessageConstants.CONTENT_CREATED, saved);
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
            ExpectedSolution saved = solutionRepo.save(sol);
            return ApiResponse.call(HttpStatus.OK, MessageConstants.CONTENT_UPDATED, saved);
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

    /** Traces question → dataset to get the moduleKey (dataType). */
    private String resolveModuleByQuestion(String questionId) {
        Question q = questionRepo.findById(questionId);
        if (q == null) return null;
        Dataset d = datasetRepo.findById(q.getDatasetId());
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
        d.setSqlModesAvailable(req.getSqlModesAvailable());
        d.setTableCount(req.getTableCount());
        return d;
    }

    private Question toQuestion(AdminQuestionRequestDTO req) {
        Question q = new Question();
        q.setDatasetId(req.getDatasetId());
        q.setTitle(req.getTitle());
        q.setQuestion(req.getQuestion());
        q.setDifficulty(req.getDifficulty());
        q.setType(req.getType());
        q.setTags(req.getTags());
        q.setTableNames(req.getTableNames());
        return q;
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
