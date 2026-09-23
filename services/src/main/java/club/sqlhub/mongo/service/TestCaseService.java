package club.sqlhub.mongo.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import club.sqlhub.Repository.TestCaseSQLRepository;
import club.sqlhub.entity.Enums.TestCaseType;
import club.sqlhub.mongo.models.TestCaseSQL.TestCase;
import club.sqlhub.mongo.models.TestCaseSQL.TestCases;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestCaseService {

    private final TestCaseSQLRepository repo;

    public TestCases create(TestCases testCases) {
        return repo.save(testCases);
    }

    public TestCases update(TestCases testCases) {
        return repo.save(testCases);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }

    public Optional<TestCases> findById(String id) {
        return repo.findById(id);
    }

    public List<TestCases> findAll() {
        return repo.findAll();
    }

    public List<TestCases> findByTestCaseType(String type) {
        return repo.findAll().stream()
                .filter(tc -> tc.getTestCases() != null &&
                        tc.getTestCases().stream().anyMatch(c -> type.equals(c.getType())))
                .collect(Collectors.toList());
    }

    public List<TestCases> findByTypeAndQuestionId(String type, String questionId) {
        return repo.findByQuestionId(questionId)
                .map(tc -> {
                    tc.setTestCases(tc.getTestCases().stream()
                            .filter(c -> type.equals(c.getType()))
                            .collect(Collectors.toList()));
                    return List.of(tc);
                })
                .orElse(Collections.emptyList());
    }

    public List<TestCase> findTestCasesByQuestionId(String questionId) {
        return repo.findByQuestionId(questionId)
                .map(TestCases::getTestCases)
                .orElse(Collections.emptyList());
    }

    public List<TestCase> findTestCasesByTypeAndQuestionId(TestCaseType type, String questionId) {
        return repo.findByQuestionId(questionId)
                .map(tc -> tc.getTestCases().stream()
                        .filter(c -> type.getDbValue().equals(c.getType()))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public String findExpectedSql(String questionId) {
        return repo.findExpectedSqlByQuestionId(questionId);
    }

    public List<TestCases> findTestCasesByTypeWithProjection(String type) {
        return findByTestCaseType(type);
    }

    public List<TestCases> findAllTypes() {
        return repo.findAll();
    }

    public List<TestCases> findByQuestionIds(List<String> questionIds) {
        return repo.findByQuestionIds(questionIds);
    }
}
