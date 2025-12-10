package club.sqlhub.mongo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import club.sqlhub.mongo.models.TestCaseSQL.TestCases;

@Repository
public interface TestcaseRepository extends MongoRepository<TestCases, String> {

    // Find test cases by type
    @Query("{ 'testCases.type': ?0 }")
    List<TestCases> findByTestCaseType(String type);

    @Query("{ 'questionId': ?0 }")
    Optional<TestCases> findByQuestionId(String questionId);

    @Query(value = "{ 'questionId': ?0 }", fields = "{ 'expectedSql': 1 }")
    Optional<TestCases> findExpectedSqlByQuestionId(String questionId);

    // Find test cases by type AND questionId
    @Query("{ 'testCases.type': ?0, 'testCases.questionId': ?1 }")
    List<TestCases> findByTypeAndQuestionId(String type, String questionId);

    // Find by type and get only matching test cases
    @Query(value = "{ 'testCases.type': ?0 }", fields = "{ 'testCases': { $elemMatch: { type: ?0 } } }")
    List<TestCases> findTestCasesByTypeWithProjection(String type);

    // Find all distinct types
    @Query(value = "{}", fields = "{ 'testCases.type': 1 }")
    List<TestCases> findAllTypes();

    // Find by multiple questionIds
    @Query("{ 'testCases.questionId': { $in: ?0 } }")
    List<TestCases> findByQuestionIds(List<String> questionIds);
}
