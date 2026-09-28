package club.sqlhub.Repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import club.sqlhub.mongo.models.Problem;
import club.sqlhub.mongo.models.Question;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuestionSQLRepository {

    private final ProblemSQLRepository problemRepo;

    private Question toQuestion(Problem p) {
        if (p == null) return null;
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
        return q;
    }

    private Problem toProblem(Question q) {
        Problem p = new Problem();
        p.setId(q.getId());
        p.setDatasetId(q.getDatasetId());
        p.setTitle(q.getTitle());
        p.setQuestion(q.getQuestion());
        p.setDifficulty(q.getDifficulty());
        p.setTags(q.getTags());
        p.setType(q.getType());
        p.setTableNames(q.getTableNames());
        p.setCreatedAt(q.getCreatedAt());
        p.setDeletedAt(q.getDeletedAt());
        return p;
    }

    public Question findById(String id) {
        return toQuestion(problemRepo.findById(id));
    }

    public List<Question> findByDatasetId(String datasetId) {
        return problemRepo.findByDatasetId(datasetId).stream()
                .map(this::toQuestion)
                .collect(Collectors.toList());
    }

    public Question save(Question q) {
        Problem saved = problemRepo.save(toProblem(q));
        q.setId(saved.getId());
        return q;
    }

    public List<Question> findAllById(Collection<String> ids) {
        return problemRepo.findAllById(ids).stream()
                .map(this::toQuestion)
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        problemRepo.deleteById(id);
    }
}
