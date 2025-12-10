package club.sqlhub.mongo.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import club.sqlhub.mongo.models.TestCaseSQL.TestCase;
import club.sqlhub.mongo.models.TestCaseSQL.TestCases;
import club.sqlhub.mongo.repository.TestcaseRepository;

@Service
public class TestCaseService {

    @Autowired
    private TestcaseRepository repo;

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

    // Query methods - return as-is from repository
    public List<TestCases> findByTestCaseType(String type) {
        return repo.findByTestCaseType(type);
    }

    public List<TestCases> findByTypeAndQuestionId(String type, String questionId) {
        return repo.findByTypeAndQuestionId(type, questionId);
    }

    public List<TestCase> findTestCasesByQuestionId(String questionId) {
        Optional<TestCases> result = repo.findByQuestionId(questionId);
        return result.map(TestCases::getTestCases)
                .orElse(Collections.emptyList());
    }

    public String findExpectedSql(String questionId) {
        Optional<TestCases> result = repo.findExpectedSqlByQuestionId(questionId);
        return result.map(TestCases::getExpectedSql)
                .orElse(null);
    }

    public List<TestCases> findTestCasesByTypeWithProjection(String type) {
        return repo.findTestCasesByTypeWithProjection(type);
    }

    public List<TestCases> findAllTypes() {
        return repo.findAllTypes();
    }

    public List<TestCases> findByQuestionIds(List<String> questionIds) {
        return repo.findByQuestionIds(questionIds);
    }
}