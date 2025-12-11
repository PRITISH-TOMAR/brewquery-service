package club.sqlhub.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;

@Repository
public interface UserQueriesRepository extends MongoRepository<JudgeResultDTO, String> {
    
    public JudgeResultDTO findByJobId(String jobId);
    public List<JudgeResultDTO> findByUserId(String userId);
}
