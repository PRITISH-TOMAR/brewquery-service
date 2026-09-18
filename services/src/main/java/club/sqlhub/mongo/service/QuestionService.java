package club.sqlhub.mongo.service;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.Datasets.ProblemDescription;
import club.sqlhub.mongo.models.Metadata;
import club.sqlhub.mongo.models.Question;
import club.sqlhub.mongo.repository.MetadataRepository;
import club.sqlhub.mongo.repository.QuestionRepository;
import club.sqlhub.utils.APiResponse.ApiResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository repo;
    private final MetadataRepository metaRepo;

    public Question getByIdRaw(String problemId) {
        return repo.findById(problemId).orElse(null);
    }

    public ResponseEntity<ApiResponse<ProblemDescription>> getById(String problemId) {
        try {
            Question ques = repo.findById(problemId).orElse(null);
            if (ques == null) {
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.NO_QUESTION_FOUND_FOR_THIS_DATASET);
            }

            String dbId = ques.getDatasetId();
            Metadata metadata = metaRepo.findById(dbId).orElse(null);

            ProblemDescription res = new ProblemDescription();
            res.setQuestion(ques);
            res.setMetadata(metadata);

            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, res);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    public ResponseEntity<ApiResponse<List<Question>>> getByDataset(String datasetId) {
        try {
            List<Question> res = repo.findByDatasetId(datasetId);

            if (res.isEmpty()) {
                return ApiResponse.error(HttpStatus.NOT_FOUND, MessageConstants.NO_QUESTION_FOUND_FOR_THIS_DATASET,
                        null);
            }

            return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, res);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
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
