package club.sqlhub.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import club.sqlhub.mongo.models.ExpectedSolution;

@Repository
public interface ExpectedSolutionRepository extends MongoRepository<ExpectedSolution, String> {

    List<ExpectedSolution> findByQuestionId(String questionId);

    List<ExpectedSolution> findByDatasetId(String datasetId);

    List<ExpectedSolution> findByQuestionIdAndSqlMode(String questionId, String sqlMode);
}
