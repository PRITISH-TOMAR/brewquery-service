package club.sqlhub.mongo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import club.sqlhub.mongo.models.Question;
import club.sqlhub.mongo.repository.QuestionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository repo;

    public Question getById(String id) {
        return repo.findById(id).orElse(null);
    }

    public List<Question> getByDataset(String datasetId) {
        return repo.findByDatasetId(datasetId);
    }

    public Question create(Question question) {
        return repo.save(question);
    }

    public Question update(Question question) {
        return repo.save(question);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }
}
