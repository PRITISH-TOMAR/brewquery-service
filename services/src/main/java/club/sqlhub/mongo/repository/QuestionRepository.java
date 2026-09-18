package club.sqlhub.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import club.sqlhub.mongo.models.Question;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {

    List<Question> findByDatasetId(String datasetId);
}
