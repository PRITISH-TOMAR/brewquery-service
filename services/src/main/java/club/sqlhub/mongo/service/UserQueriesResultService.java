package club.sqlhub.mongo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.mongo.repository.UserQueriesRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserQueriesResultService {

    private final UserQueriesRepository repo;

    public JudgeResultDTO findById(String jobId) {
        JudgeResultDTO res = repo.findByJobId(jobId);
        return res;
    }

    public List<JudgeResultDTO> findByUserId(String userId) {
        return repo.findByUserId(userId);
    }

}